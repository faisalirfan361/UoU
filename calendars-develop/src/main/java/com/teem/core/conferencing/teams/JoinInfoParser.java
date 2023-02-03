package com.UoU.core.conferencing.teams;

import com.microsoft.graph.models.OnlineMeeting;
import com.UoU.core.accounts.Provider;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.val;

/**
 * Package-private helper for parsing meeting join info HTML and appending it to existing text/HTML.
 */
class JoinInfoParser {

  /**
   * Prefix for HTML data URLs returned by the Graph API for url-encoded HTML.
   */
  private static final String HTML_DATA_URL_PREFIX = "data:text/html,";

  /**
   * Teams product name that is never translated in MS HTML (so we can use to find the heading).
   */
  private static final String HEADING_PRODUCT = "Microsoft Teams";

  /**
   * Matches the Teams heading text inside MS HTML, which is language-specific except product name.
   */
  private static final Pattern HEADING_PATTERN = Pattern.compile(
      "<span[^>]*>\\s*(.*)" + HEADING_PRODUCT + "(.*)\\s*<\\/span>",
      Pattern.CASE_INSENSITIVE);

  /**
   * Default heading text for result for when localized text cannot be parsed.
   */
  private static final String HEADING_DEFAULT_TEXT = HEADING_PRODUCT + " meeting";

  /**
   * Matches the join link text inside MS HTML, which is language-specific.
   */
  private static final Pattern JOIN_LINK_PATTERN = Pattern.compile(
      "<a[^>]* class=\"me-email-headline\"[^>]*>\\s*(.+)\\s*<\\/a>",
      Pattern.CASE_INSENSITIVE);

  /**
   * Default join meeting link text for result, which matches MS HTML for English.
   */
  private static final String JOIN_LINK_DEFAULT_TEXT = "Click here to join the meeting";

  /**
   * Matches the Meeting ID text in MS HTML, which is language-specific.
   */
  private static final Pattern MEETING_ID_PATTERN = Pattern.compile(
      "<span[^>]* data-tid=\"meeting-code\"[^>]*>[\\s]*(.+)[\\s]*<span[^>]*>([\\d ]+)<\\/span>",
      Pattern.CASE_INSENSITIVE);

  /**
   * HTML result format for a join info section.
   */
  private static final String JOIN_INFO_FORMAT = """

      <div>__________________________</div><div><strong>%s</strong><br><a href="%s" target="_blank" rel="noreferrer noopener">%s</a></div>
      <div>%s</div>
      """;

  /**
   * Matches a full HTML doc and captures two groups so new HTML can be inserted into the body.
   */
  private static final Pattern HTML_DOC_INSERT_PATTERN = Pattern.compile(
      "^\\s*(<html[^>]*>[\\s\\S]*<body[^>]*>[\\s\\S]*)(</body>[\\s\\S]*</html>)\\s*$",
      Pattern.CASE_INSENSITIVE);

  /**
   * Format to place new content between the captured groups from {@link #HTML_DOC_INSERT_PATTERN}.
   */
  private static final String HTML_DOC_INSERT_REPLACEMENT_FORMAT = "$1%s$2";

  /**
   * Parses meeting join info and returns HTML suitable for the provider.
   *
   * <p>The provider is necessary because the MS OnlineMeeting HTML will be simplified for Google
   * so it looks ok in the Google Calendar UI (Google only supports basic HTML).
   *
   * <p>For Google, the result will be something like:
   * <pre>{@code
   * <div><strong>Microsoft Teams meeting</strong><br><a href="https://example.com" target="_blank" rel="noreferrer noopener">Click here to join the meeting</a></div>
   * <div>Meeting ID: 247 652 109 526</div>
   * }</pre>
   *
   * <p>For all other calendars, the result will be the original MS OnlineMeeting HTML.
   */
  public String parseJoinInfoHtml(OnlineMeeting meeting, Provider provider) {
    val html = decodeJoinHtml(meeting);

    // For Google, return simplified HTML.
    if (provider == Provider.GOOGLE) {
      val headingText = parseHeadingText(html).orElse(HEADING_DEFAULT_TEXT);
      val joinLinkText = parseJoinLinkText(html).orElse(JOIN_LINK_DEFAULT_TEXT);
      val meetingIdText = parseMeetingIdText(html).orElse("");
      return JOIN_INFO_FORMAT.formatted(
          headingText, meeting.joinWebUrl, joinLinkText, meetingIdText);
    }

    // For Microsoft and other providers, return HTML as is from the Graph API.
    return html;
  }

  /**
   * Helper that appends {@link #parseJoinInfoHtml(OnlineMeeting, Provider)} to the input string.
   *
   * <p>If the input is a full HTML doc, the join HTML will be inserted into the body. Otherwise,
   * the join HTML will be appended to the input.
   */
  public String appendJoinInfoHtml(String input, OnlineMeeting meeting, Provider provider) {
    var result = parseJoinInfoHtml(meeting, provider);

    if (input != null && !input.isBlank()) {
      // If the input is a full html doc, insert join html into body. Else just append html.
      val htmlDocMatcher = HTML_DOC_INSERT_PATTERN.matcher(input);
      if (htmlDocMatcher.matches()) {
        result = htmlDocMatcher.replaceFirst(HTML_DOC_INSERT_REPLACEMENT_FORMAT.formatted(result));
      } else {
        result = input + result;
      }
    }

    return result.trim();
  }

  /**
   * Decodes meeting HTML from the data URL format, which is URL encoded.
   */
  private static String decodeJoinHtml(OnlineMeeting meeting) {
    if (meeting.joinInformation.content == null
        || !meeting.joinInformation.content.startsWith(HTML_DATA_URL_PREFIX)) {
      throw new IllegalArgumentException("Invalid Teams joinInformation data URL");
    }

    val urlEncodedData = meeting.joinInformation.content.substring(HTML_DATA_URL_PREFIX.length());
    return URLDecoder.decode(urlEncodedData, StandardCharsets.UTF_8);
  }

  /**
   * Parses localized heading text from MS HTML.
   */
  private static Optional<String> parseHeadingText(String html) {
    val matcher = HEADING_PATTERN.matcher(html);
    return matcher.find()
        ? Optional.of(matcher.group(1) + HEADING_PRODUCT + matcher.group(2))
        : Optional.empty();
  }

  /**
   * Parses localized join meeting link text from MS HTML.
   */
  private static Optional<String> parseJoinLinkText(String html) {
    val matcher = JOIN_LINK_PATTERN.matcher(html);
    return matcher.find()
        ? Optional.of(matcher.group(1))
        : Optional.empty();
  }

  /**
   * Parses localized meeting id text from MS HTML.
   */
  private static Optional<String> parseMeetingIdText(String html) {
    val matcher = MEETING_ID_PATTERN.matcher(html);
    return matcher.find()
        ? Optional.of(matcher.group(1).trim() + " " + matcher.group(2).trim())
        : Optional.empty();
  }
}
