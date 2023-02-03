package com.UoU._helpers;

import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.OnlineMeeting;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.TimeSpan;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthResult;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.InternalCalendarsConfig;
import com.UoU.core.conferencing.ConferencingAuthInfo;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventsConfig;
import com.UoU.core.events.Owner;
import com.UoU.core.events.Participant;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.val;

/**
 * Helper for creating test data more easily for things we create a lot in tests.
 *
 * <p>Everything returned by this class will be unique and random where suitable. For example,
 * strings, emails, and ids are all random and unique. However, dates and times will not be unique.
 */
public class TestData {

  private static final Random RANDOM = new Random();
  private static final DateTimeFormatter RECURRENCE_DATETIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyyMMdd'T'HHmmss")
      .withZone(ZoneOffset.UTC);

  public static String uuidString() {
    return UUID.randomUUID().toString();
  }

  public static String email() {
    return UUID.randomUUID() + "@example.com";
  }

  public static String emailStartingWith(String prefix) {
    return prefix + "." + email();
  }

  public static OrgId orgId() {
    return new OrgId(uuidString());
  }

  public static AccountId accountId() {
    return new AccountId(uuidString());
  }

  public static ServiceAccountId serviceAccountId() {
    return ServiceAccountId.create();
  }

  public static CalendarExternalId calendarExternalId() {
    return new CalendarExternalId(uuidString());
  }

  public static EventExternalId eventExternalId() {
    return new EventExternalId(uuidString());
  }

  public static Instant instant() {
    return Instant.now();
  }

  public static LocalDate localDate() {
    return LocalDate.now();
  }

  public static TimeSpan timeSpan() {
    val startSeconds = RANDOM.nextInt((int) Duration.ofDays(30).toSeconds());
    return new TimeSpan(
        Instant.now().plusSeconds(startSeconds),
        Instant.now().plusSeconds(startSeconds + 900));
  }

  public static When.TimeSpan whenTimeSpan() {
    val startSeconds = RANDOM.nextInt((int) Duration.ofDays(30).toSeconds());
    return ModelBuilders.whenTimeSpan()
        .startTime(Instant.now().plusSeconds(startSeconds))
        .endTime(Instant.now().plusSeconds(startSeconds + 1800))
        .build();
  }

  public static When.DateSpan whenDateSpan() {
    val startDays = RANDOM.nextInt(30);
    return ModelBuilders.whenDateSpan()
        .startDate(LocalDate.now().plusDays(startDays))
        .endDate(LocalDate.now().plusDays(startDays + 2))
        .build();
  }

  public static When.Date whenDate() {
    return new When.Date(LocalDate.now().plusDays(RANDOM.nextInt(30)));
  }

  public static String timezone() {
    val timezones = TimeZone.getAvailableIDs();
    val randomIndex = RANDOM.nextInt(0, timezones.length);
    return timezones[randomIndex];
  }

  public static String timezoneOtherThan(String notTimezone) {
    var result = notTimezone;
    while (result == null || result.equals(notTimezone)) {
      result = timezone();
    }
    return result;
  }

  public static SecretString secretString() {
    return new SecretString(uuidString());
  }

  public static Event event() {
    return ModelBuilders.eventWithTestData().build();
  }

  public static Owner owner() {
    var email = TestData.email();
    return new Owner("Owner " + email, email);
  }

  public static ParticipantRequest participantRequest() {
    return participantRequestList(1).get(0);
  }

  public static List<ParticipantRequest> participantRequestList(int count) {
    var emailSuffix = "@" + uuidString() + ".example.com";
    return IntStream
        .range(1, Math.max(1, count) + 1)
        .boxed()
        .map(n -> ParticipantRequest.builder()
            .name("Participant " + n)
            .email("participant" + n + emailSuffix)
            .build())
        .toList();
  }

  public static List<Participant> participantList(int count) {
    var emailSuffix = "@" + uuidString() + ".example.com";
    return IntStream
        .range(1, Math.max(1, count) + 1)
        .boxed()
        .map(n -> ModelBuilders.participant()
            .name("Participant " + n)
            .email("participant" + n + emailSuffix)
            .build())
        .toList();
  }

  public static Recurrence recurrenceMaster() {
    val until = Instant.now().plusSeconds(Duration.ofDays(RANDOM.nextInt(1095)).toSeconds());
    return Recurrence.master(
        List.of("RRULE:FREQ=DAILY;UNTIL=" + RECURRENCE_DATETIME_FORMATTER.format(until)),
        timezone());
  }

  public static Recurrence recurrenceInstance() {
    return Recurrence.instance(EventId.create(), false);
  }

  public static EventsConfig eventsConfig() {
    return new EventsConfig(new EventsConfig.ActivePeriod(33, 99));
  }

  public static OauthConfig oauthConfig() {
    return new OauthConfig(
        "https://example.com",
        new OauthConfig.OauthCredentials("id", new SecretString("secret")),
        new OauthConfig.OauthCredentials("id", new SecretString("secret")));
  }

  public static OauthResult oauthResult(String email) {
    return new OauthResult(
        "name", email, new SecretString("refresh"), new SecretString("access"), null);
  }

  public static com.nylas.Event nylasEvent() {
    val str = TestData.uuidString();
    return Fluent
        .of(new com.nylas.Event(
            CalendarId.create().value(), new com.nylas.Event.Date(LocalDate.now())))
        .also(x -> x.setTitle(str))
        .also(x -> x.setDescription(str))
        .also(x -> x.setLocation(str))
        .also(x -> x.setBusy(false))
        .also(x -> x.setMetadata(Map.of("meta1", str, "meta2", str)))
        .also(x -> x.setParticipants(List.of(new com.nylas.Participant(TestData.email()))))
        .get();
  }

  public static InternalCalendarsConfig internalCalendarsConfig() {
    return new InternalCalendarsConfig("-calendar@test-" + UUID.randomUUID());
  }

  public static ConferencingAuthInfo conferencingAuthInfo() {
    return new ConferencingAuthInfo(
        "Test Person ",
        TestData.secretString(),
        TestData.secretString(),
        Instant.now().plus(30, ChronoUnit.MINUTES));
  }

  public static OnlineMeeting teamsMeeting() {
    return teamsMeeting(
        "data:text/html,%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e%0d%0a+%0d%0a+%3cdiv+class%3d%22me-email-text%22+style%3d%22color%3a%23252424%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+lang%3d%22en-US%22%3e%0d%0a++++%3cdiv+style%3d%22margin-top%3a+24px%3b+margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cspan+style%3d%22font-size%3a+24px%3b+color%3a%23252424%22%3eMicrosoft+Teams+meeting%3c%2fspan%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cdiv+style%3d%22margin-top%3a+0px%3b+margin-bottom%3a+0px%3b+font-weight%3a+bold%22%3e%0d%0a++++++++++%3cspan+style%3d%22font-size%3a+14px%3b+color%3a%23252424%22%3eJoin+on+your+computer%2c+mobile+app+or+room+device%3c%2fspan%3e%0d%0a++++++++%3c%2fdiv%3e%0d%0a++++++++%3ca+class%3d%22me-email-headline%22+style%3d%22font-size%3a+14px%3bfont-family%3a%27Segoe+UI+Semibold%27%2c%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3b%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fl%2fmeetup-join%2f19%253ameeting_OWY1OTJlNGMtMzg0Zi00MmY1LTg2YWEtZjU4ZjMzZDg3N2Q3%2540thread.v2%2f0%3fcontext%3d%257b%2522Tid%2522%253a%25229c62192c-c766-43eb-adc8-a0cbe67f8085%2522%252c%2522Oid%2522%253a%25225d1b9abc-a510-4135-94a2-a89b19cf14ce%2522%257d%22+target%3d%22_blank%22+rel%3d%22noreferrer+noopener%22%3eClick+here+to+join+the+meeting%3c%2fa%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a20px%3b+margin-top%3a20px%22%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a4px%22%3e%0d%0a++++++++%3cspan+data-tid%3d%22meeting-code%22+style%3d%22font-size%3a+14px%3b+color%3a%23252424%3b%22%3e%0d%0a++++++++++++Meeting+ID%3a+%3cspan+style%3d%22font-size%3a16px%3b+color%3a%23252424%3b%22%3e247+652+109+526%3c%2fspan%3e%0d%0a+++++++%3c%2fspan%3e%0d%0a++++++++%0d%0a++++++++%3cdiv+style%3d%22font-size%3a+14px%3b%22%3e%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fen-us%2fmicrosoft-teams%2fdownload-app%22+rel%3d%22noreferrer+noopener%22%3e%0d%0a++++++++Download+Teams%3c%2fa%3e+%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fmicrosoft-teams%2fjoin-a-meeting%22+rel%3d%22noreferrer+noopener%22%3eJoin+on+the+web%3c%2fa%3e%3c%2fdiv%3e%0d%0a++++%3c%2fdiv%3e%0d%0a+%3c%2fdiv%3e%0d%0a++++%0d%0a++++++%0d%0a++++%0d%0a++++%0d%0a++++%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+24px%3bmargin-top%3a+20px%3b%22%3e%0d%0a++++++++%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2faka.ms%2fJoinTeamsMeeting%22+rel%3d%22noreferrer+noopener%22%3eLearn+More%3c%2fa%3e++%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fmeetingOptions%2f%3forganizerId%3d5d1b9abc-a510-4135-94a2-a89b19cf14ce%26tenantId%3d9c62192c-c766-43eb-adc8-a0cbe67f8085%26threadId%3d19_meeting_OWY1OTJlNGMtMzg0Zi00MmY1LTg2YWEtZjU4ZjMzZDg3N2Q3%40thread.v2%26messageId%3d0%26language%3den-US%22+rel%3d%22noreferrer+noopener%22%3eMeeting+options%3c%2fa%3e+%0d%0a++++++%3c%2fdiv%3e%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b+margin-bottom%3a+4px%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+12px%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e");
  }

  public static OnlineMeeting teamsMeeting(String joinInfoContent) {
    return teamsMeeting(joinInfoContent, "https://example.com");
  }

  public static OnlineMeeting teamsMeeting(String joinInfoContent, String joinWebUrl) {
    val meeting = new OnlineMeeting();
    meeting.id = uuidString();
    meeting.subject = "Fake " + meeting.id;
    meeting.joinInformation = new ItemBody();
    meeting.joinInformation.contentType = BodyType.HTML;
    meeting.joinInformation.content = joinInfoContent;
    meeting.joinWebUrl = joinWebUrl;
    return meeting;
  }
}
