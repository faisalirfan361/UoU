package com.UoU.core.conferencing.teams;

import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingCreateOrGetParameterSet;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.requests.GraphServiceClient;
import com.UoU.core.Fluent;
import com.UoU.core.accounts.Provider;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.events.EventCreateRequest;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.val;
import okhttp3.Request;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Service for MS Teams Graph API calls.
 *
 * <p>This wraps the MS Graph SDK and sets up auth based on ConferencingUserId. Use this instead of
 * using Graph directly, and Graph calls will be automatically authorized.
 *
 * <p>Usually, you'll want to use {@link com.UoU.core.conferencing.ConferencingService} instead
 * of calling this directly because that service is provider-agnostic.
 */
@Service
@AllArgsConstructor
public class TeamsService {

  private static final String MEETING_EXTERNAL_ID_PREFIX = "UoUCalendarsApiEvent:";
  private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
  private static final String HEADER_ACCEPT_LANGUAGE_DEFAULT = Locale.ENGLISH.getLanguage();
  private static final String LANGUAGE_UNDETERMINED = "und";

  private final GraphServiceClient<Request> graphServiceClient;
  private final HttpHeaderManager httpHeaderManager = new HttpHeaderManager(); // private dep
  private final JoinInfoParser joinInfoParser = new JoinInfoParser(); // private dep

  /**
   * Adds Teams meeting info to the event description and returns a new event create request.
   *
   * <p>This calls {@link #createOrGetMeeting(ConferencingUserId, Locale, Function)} to create the
   * Teams meeting.
   *
   * @param request The original event create request.
   * @param conferencingUserId The user that will be used to create the Teams meeting.
   * @param calendarTimeZoneSupplier The timezone to use for interpreting all-day event times.
   * @param provider The account provider, which determines HTML output so it looks good everywhere.
   * @param locale The language to use when requesting join info from Teams, such as en-US.
   */
  public EventCreateRequest addConferencingToEvent(
      EventCreateRequest request,
      ConferencingUserId conferencingUserId,
      Supplier<ZoneId> calendarTimeZoneSupplier,
      Provider provider,
      @Nullable Locale locale) {

    val meeting = createOrGetMeeting(conferencingUserId, locale, params -> {
      params = params
          .withExternalId(MEETING_EXTERNAL_ID_PREFIX + request.id().value())
          .withSubject(request.title());

      val timeSpan = request.when().toUtcTimeSpan(calendarTimeZoneSupplier);
      params = params
          .withStartDateTime(timeSpan.startAtUtcOffset())
          .withEndDateTime(timeSpan.endAtUtcOffset());

      val participants = new MeetingParticipants();
      participants.attendees = Optional
          .ofNullable(request.participants())
          .map(list -> list.stream())
          .orElse(Stream.of())
          .map(p -> Fluent.of(new MeetingParticipantInfo())
              .also(mpi -> mpi.role = OnlineMeetingRole.ATTENDEE)
              .also(mpi -> mpi.upn = p.email())
              .get())
          .toList();
      params = params.withParticipants(participants);

      return params.build();
    });

    val newDescription = joinInfoParser.appendJoinInfoHtml(
        request.description(), meeting, provider);

    return request.toBuilder().description(newDescription).build();
  }

  /**
   * Sends an OnlineMeetingCreateOrGet request with auth for the ConferencingUserId.
   *
   * <p>The locale is used for the Accept-Language header, which controls joinInformation text.
   */
  public OnlineMeeting createOrGetMeeting(
      ConferencingUserId userId,
      @Nullable Locale locale,
      Function<
          OnlineMeetingCreateOrGetParameterSet.OnlineMeetingCreateOrGetParameterSetBuilder,
          OnlineMeetingCreateOrGetParameterSet> paramsCreator) {

    val params = paramsCreator.apply(OnlineMeetingCreateOrGetParameterSet.newBuilder());
    val request = buildRequest(userId, graph -> graph
        .me()
        .onlineMeetings()
        .createOrGet(params)
        .buildRequest());

    addAcceptLanguageHeader(request, locale);

    return request.post();
  }

  /**
   * Helper to build a request that's setup for auth with our custom interceptor.
   */
  private <T extends IHttpRequest> T buildRequest(
      ConferencingUserId conferencingUserId,
      Function<GraphServiceClient<Request>, T> builder) {

    val request = builder.apply(graphServiceClient);
    httpHeaderManager.addRequireAuthHeader(request, conferencingUserId);
    return request;
  }

  /**
   * Adds an Accept-Language header to the API request for endpoints that support localization.
   *
   * <p>Subtags will be added if needed to support more general locales and our default language.
   * For example, if the locale language tag is "fr-CH", the end result would be "fr-CH,fr,en" so
   * that fallbacks are requested. See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language syntax</a>
   * for details.
   */
  private static void addAcceptLanguageHeader(IHttpRequest request, @Nullable Locale locale) {
    val items = new LinkedHashSet<String>();

    if (locale != null) {
      val languageTag = locale.toLanguageTag();
      if (!languageTag.equals(LANGUAGE_UNDETERMINED)) {
        items.add(locale.toLanguageTag());
      }

      val languageOnly = locale.getLanguage();
      if (!items.contains(languageOnly)) {
        items.add(languageOnly);
      }
    }

    // If no valid locale was requested, skip adding header so that default for user applies.
    if (items.isEmpty()) {
      return;
    }

    // But if were requesting a locale, make sure to fallback to our default.
    if (!items.contains(HEADER_ACCEPT_LANGUAGE_DEFAULT)) {
      items.add(HEADER_ACCEPT_LANGUAGE_DEFAULT);
    }

    request.addHeader(HEADER_ACCEPT_LANGUAGE, String.join(",", items));
  }
}
