package com.UoU.core.conferencing.teams;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.core.accounts.Provider;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JoinInfoParserTests {
  private static final JoinInfoParser PARSER = new JoinInfoParser();

  @Test
  void parseJoinInfoHtml_shoulReturnDecodedOriginalHtmlForMicrosoft() {
    val scenario = Scenario.DEFAULT;
    val meeting = TestData.teamsMeeting(scenario.contentDataUrl);

    val result = PARSER.appendJoinInfoHtml(null, meeting, Provider.MICROSOFT);

    assertThat(result)
        .as("HTML should be URL decoded and contain meeting id")
        .contains("<span data-tid=\"meeting-code\"");
  }

  @ParameterizedTest
  @EnumSource(Scenario.class)
  void parseJoinInfoHtml_shouldSimplifyLocalizedHtmlForGoogle(Scenario scenario) {
    val meeting = TestData.teamsMeeting(scenario.contentDataUrl);

    val result = PARSER.appendJoinInfoHtml(null, meeting, Provider.GOOGLE);

    // Simplified result HTML is something like:
    // <div><strong>Microsoft Teams meeting</strong><br><a href="https://example.com" target="_blank" rel="noreferrer noopener">Click here to join the meeting</a></div>
    // <div>Meeting ID: 247 652 109 526</div>

    assertThat(result).contains(">" + scenario.headingText + "</");
    assertThat(result).contains("<a href=\"" + meeting.joinWebUrl + "\"");
    assertThat(result).contains(">" + scenario.linkText + "</a>");
    assertThat(result).contains(">" + scenario.meetingIdText + "</");
  }

  @Test
  void appendJoinInfoHtml_shouldReplaceBlankInput() {
    val input = " ";
    val meeting = TestData.teamsMeeting();
    val joinInfoHtml = PARSER.parseJoinInfoHtml(meeting, Provider.INTERNAL);
    val result = PARSER.appendJoinInfoHtml(input, meeting, Provider.INTERNAL);

    assertThat(result).isEqualTo(joinInfoHtml);
  }

  @Test
  void appendJoinInfoHtml_shouldAppendToInputAndTrim() {
    val input = "   Existing string   ";
    val meeting = TestData.teamsMeeting();
    val joinInfoHtml = PARSER.parseJoinInfoHtml(meeting, Provider.INTERNAL);
    val result = PARSER.appendJoinInfoHtml(input, meeting, Provider.INTERNAL);

    assertThat(result).isEqualTo((input + joinInfoHtml).trim());
  }

  @Test
  void appendJoinInfoHtml_shouldAppendInputToSimpleHtmlDoc() {
    val input = "<HTML><body>existing</body></HTML>";
    val meeting = TestData.teamsMeeting();
    val joinInfoHtml = PARSER.parseJoinInfoHtml(meeting, Provider.INTERNAL);
    val result = PARSER.appendJoinInfoHtml(input, meeting, Provider.INTERNAL);

    assertThat(result).isEqualTo("<HTML><body>existing" + joinInfoHtml + "</body></HTML>");
  }

  @Test
  void appendJoinInfoHtml_shouldAppendInputToComplexHtmlDoc() {
    val input = """
        \r\n
        <html lang="en">
        <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        </head>
        <body class="body">
          <div>existing</div>
        </body>
        </html>
        \t\t\t\r\n
        """;
    val meeting = TestData.teamsMeeting();
    val joinInfoHtml = PARSER.parseJoinInfoHtml(meeting, Provider.INTERNAL);
    val result = PARSER.appendJoinInfoHtml(input, meeting, Provider.INTERNAL);

    assertThat(result).startsWith("<html lang=\"en\">"); // initial whitespace is trimmed
    assertThat(result).containsIgnoringWhitespaces(
        "<body class=\"body\"><div>existing</div>" + joinInfoHtml + "</body>");
    assertThat(result).endsWith("</html>"); // end whitespace is trimmed
  }

  /**
   * Join info scenarios based on localized HTML actually fetched from the MS Graph API.
   */
  @AllArgsConstructor
  private enum Scenario {
    DEFAULT(
        "data:text/html,%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e%0d%0a+%0d%0a+%3cdiv+class%3d%22me-email-text%22+style%3d%22color%3a%23252424%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+lang%3d%22en-US%22%3e%0d%0a++++%3cdiv+style%3d%22margin-top%3a+24px%3b+margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cspan+style%3d%22font-size%3a+24px%3b+color%3a%23252424%22%3eMicrosoft+Teams+meeting%3c%2fspan%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cdiv+style%3d%22margin-top%3a+0px%3b+margin-bottom%3a+0px%3b+font-weight%3a+bold%22%3e%0d%0a++++++++++%3cspan+style%3d%22font-size%3a+14px%3b+color%3a%23252424%22%3eJoin+on+your+computer%2c+mobile+app+or+room+device%3c%2fspan%3e%0d%0a++++++++%3c%2fdiv%3e%0d%0a++++++++%3ca+class%3d%22me-email-headline%22+style%3d%22font-size%3a+14px%3bfont-family%3a%27Segoe+UI+Semibold%27%2c%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3b%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fl%2fmeetup-join%2f19%253ameeting_OWY1OTJlNGMtMzg0Zi00MmY1LTg2YWEtZjU4ZjMzZDg3N2Q3%2540thread.v2%2f0%3fcontext%3d%257b%2522Tid%2522%253a%25229c62192c-c766-43eb-adc8-a0cbe67f8085%2522%252c%2522Oid%2522%253a%25225d1b9abc-a510-4135-94a2-a89b19cf14ce%2522%257d%22+target%3d%22_blank%22+rel%3d%22noreferrer+noopener%22%3eClick+here+to+join+the+meeting%3c%2fa%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a20px%3b+margin-top%3a20px%22%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a4px%22%3e%0d%0a++++++++%3cspan+data-tid%3d%22meeting-code%22+style%3d%22font-size%3a+14px%3b+color%3a%23252424%3b%22%3e%0d%0a++++++++++++Meeting+ID%3a+%3cspan+style%3d%22font-size%3a16px%3b+color%3a%23252424%3b%22%3e247+652+109+526%3c%2fspan%3e%0d%0a+++++++%3c%2fspan%3e%0d%0a++++++++%0d%0a++++++++%3cdiv+style%3d%22font-size%3a+14px%3b%22%3e%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fen-us%2fmicrosoft-teams%2fdownload-app%22+rel%3d%22noreferrer+noopener%22%3e%0d%0a++++++++Download+Teams%3c%2fa%3e+%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fmicrosoft-teams%2fjoin-a-meeting%22+rel%3d%22noreferrer+noopener%22%3eJoin+on+the+web%3c%2fa%3e%3c%2fdiv%3e%0d%0a++++%3c%2fdiv%3e%0d%0a+%3c%2fdiv%3e%0d%0a++++%0d%0a++++++%0d%0a++++%0d%0a++++%0d%0a++++%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+24px%3bmargin-top%3a+20px%3b%22%3e%0d%0a++++++++%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2faka.ms%2fJoinTeamsMeeting%22+rel%3d%22noreferrer+noopener%22%3eLearn+More%3c%2fa%3e++%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fmeetingOptions%2f%3forganizerId%3d5d1b9abc-a510-4135-94a2-a89b19cf14ce%26tenantId%3d9c62192c-c766-43eb-adc8-a0cbe67f8085%26threadId%3d19_meeting_OWY1OTJlNGMtMzg0Zi00MmY1LTg2YWEtZjU4ZjMzZDg3N2Q3%40thread.v2%26messageId%3d0%26language%3den-US%22+rel%3d%22noreferrer+noopener%22%3eMeeting+options%3c%2fa%3e+%0d%0a++++++%3c%2fdiv%3e%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b+margin-bottom%3a+4px%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+12px%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e",
        "Microsoft Teams meeting", // heading text
        "Click here to join the meeting", // link text
        "Meeting ID: 247 652 109 526"), // meeting id text
    JAPANESE(
        "data:text/html,%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e%0d%0a+%0d%0a+%3cdiv+class%3d%22me-email-text%22+style%3d%22color%3a%23252424%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+lang%3d%22ja%22%3e%0d%0a++++%3cdiv+style%3d%22margin-top%3a+24px%3b+margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cspan+style%3d%22font-size%3a+24px%3b+color%3a%23252424%22%3eMicrosoft+Teams+%e3%83%9f%e3%83%bc%e3%83%86%e3%82%a3%e3%83%b3%e3%82%b0%3c%2fspan%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cdiv+style%3d%22margin-top%3a+0px%3b+margin-bottom%3a+0px%3b+font-weight%3a+bold%22%3e%0d%0a++++++++++%3cspan+style%3d%22font-size%3a+14px%3b+color%3a%23252424%22%3e%e3%82%b3%e3%83%b3%e3%83%94%e3%83%a5%e3%83%bc%e3%82%bf%e3%80%81%e3%83%a2%e3%83%90%e3%82%a4%e3%83%ab%e3%82%a2%e3%83%97%e3%83%aa%e3%82%b1%e3%83%bc%e3%82%b7%e3%83%a7%e3%83%b3%e3%80%81%e3%81%be%e3%81%9f%e3%81%af%e3%83%ab%e3%83%bc%e3%83%a0%e3%83%87%e3%83%90%e3%82%a4%e3%82%b9%e3%81%a7%e5%8f%82%e5%8a%a0%e3%81%99%e3%82%8b%3c%2fspan%3e%0d%0a++++++++%3c%2fdiv%3e%0d%0a++++++++%3ca+class%3d%22me-email-headline%22+style%3d%22font-size%3a+14px%3bfont-family%3a%27Segoe+UI+Semibold%27%2c%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3b%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fl%2fmeetup-join%2f19%253ameeting_MjgwMzdmZDctNjIwNi00ZDg1LTg5YjUtYTYzNDQwMzNiMDE1%2540thread.v2%2f0%3fcontext%3d%257b%2522Tid%2522%253a%25229c62192c-c766-43eb-adc8-a0cbe67f8085%2522%252c%2522Oid%2522%253a%25225d1b9abc-a510-4135-94a2-a89b19cf14ce%2522%257d%22+target%3d%22_blank%22+rel%3d%22noreferrer+noopener%22%3e%e3%81%93%e3%81%93%e3%82%92%e3%82%af%e3%83%aa%e3%83%83%e3%82%af%e3%81%97%e3%81%a6%e4%bc%9a%e8%ad%b0%e3%81%ab%e5%8f%82%e5%8a%a0%e3%81%97%e3%81%a6%e3%81%8f%e3%81%a0%e3%81%95%e3%81%84%3c%2fa%3e%0d%0a++++%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22margin-bottom%3a20px%3b+margin-top%3a20px%22%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a4px%22%3e%0d%0a++++++++%3cspan+data-tid%3d%22meeting-code%22+style%3d%22font-size%3a+14px%3b+color%3a%23252424%3b%22%3e%0d%0a++++++++++++%e4%bc%9a%e8%ad%b0+ID%3a+%3cspan+style%3d%22font-size%3a16px%3b+color%3a%23252424%3b%22%3e295+059+665+934%3c%2fspan%3e%0d%0a+++++++%3c%2fspan%3e%0d%0a%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b%22%3e%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fen-us%2fmicrosoft-teams%2fdownload-app%22+rel%3d%22noreferrer+noopener%22%3e%0d%0a++++++++Teams+%e3%81%ae%e3%83%80%e3%82%a6%e3%83%b3%e3%83%ad%e3%83%bc%e3%83%89%3c%2fa%3e+%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fmicrosoft-teams%2fjoin-a-meeting%22+rel%3d%22noreferrer+noopener%22%3eWeb+%e3%81%ab%e5%8f%82%e5%8a%a0%3c%2fa%3e%3c%2fdiv%3e%0d%0a++++%3c%2fdiv%3e%0d%0a+%3c%2fdiv%3e%0d%0a%0d%0a%0d%0a%0d%0a%0d%0a%0d%0a%3cdiv+style%3d%22margin-bottom%3a+24px%3bmargin-top%3a+20px%3b%22%3e%0d%0a++++++++%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2faka.ms%2fJoinTeamsMeeting%22+rel%3d%22noreferrer+noopener%22%3e%e8%a9%b3%e7%b4%b0%e6%83%85%e5%a0%b1%e3%83%98%e3%83%ab%e3%83%97%3c%2fa%3e++%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fmeetingOptions%2f%3forganizerId%3d5d1b9abc-a510-4135-94a2-a89b19cf14ce%26tenantId%3d9c62192c-c766-43eb-adc8-a0cbe67f8085%26threadId%3d19_meeting_MjgwMzdmZDctNjIwNi00ZDg1LTg5YjUtYTYzNDQwMzNiMDE1%40thread.v2%26messageId%3d0%26language%3dja%22+rel%3d%22noreferrer+noopener%22%3e%e4%bc%9a%e8%ad%b0%e3%81%ae%e3%82%aa%e3%83%97%e3%82%b7%e3%83%a7%e3%83%b3%3c%2fa%3e+%0d%0a++++++%3c%2fdiv%3e%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b+margin-bottom%3a+4px%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+12px%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e",
        "Microsoft Teams ミーティング",
        "ここをクリックして会議に参加してください",
        "会議 ID: 295 059 665 934"),
    SPANISH(
        "data:text/html,%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e%0d%0a+%0d%0a+%3cdiv+class%3d%22me-email-text%22+style%3d%22color%3a%23252424%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+lang%3d%22es%22%3e%0d%0a++++%3cdiv+style%3d%22margin-top%3a+24px%3b+margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cspan+style%3d%22font-size%3a+24px%3b+color%3a%23252424%22%3eReuni%c3%b3n+de+Microsoft+Teams%3c%2fspan%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cdiv+style%3d%22margin-top%3a+0px%3b+margin-bottom%3a+0px%3b+font-weight%3a+bold%22%3e%0d%0a++++++++++%3cspan+style%3d%22font-size%3a+14px%3b+color%3a%23252424%22%3e%c3%9anase+a+trav%c3%a9s+de+su+ordenador%2c+aplicaci%c3%b3n+m%c3%b3vil+o+dispositivo+de+sala%3c%2fspan%3e%0d%0a++++++++%3c%2fdiv%3e%0d%0a++++++++%3ca+class%3d%22me-email-headline%22+style%3d%22font-size%3a+14px%3bfont-family%3a%27Segoe+UI+Semibold%27%2c%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3b%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fl%2fmeetup-join%2f19%253ameeting_MmY3OTE3MzAtYWYwYi00NjYwLWEyNTMtMDljMTJkYzkyYTNl%2540thread.v2%2f0%3fcontext%3d%257b%2522Tid%2522%253a%25229c62192c-c766-43eb-adc8-a0cbe67f8085%2522%252c%2522Oid%2522%253a%25225d1b9abc-a510-4135-94a2-a89b19cf14ce%2522%257d%22+target%3d%22_blank%22+rel%3d%22noreferrer+noopener%22%3eHaga+clic+aqu%c3%ad+para+unirse+a+la+reuni%c3%b3n%3c%2fa%3e%0d%0a++++%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22margin-bottom%3a20px%3b+margin-top%3a20px%22%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a4px%22%3e%0d%0a++++++++%3cspan+data-tid%3d%22meeting-code%22+style%3d%22font-size%3a+14px%3b+color%3a%23252424%3b%22%3e%0d%0a++++++++++++ID+de+la+reuni%c3%b3n%3a+%3cspan+style%3d%22font-size%3a16px%3b+color%3a%23252424%3b%22%3e262+269+681+245%3c%2fspan%3e%0d%0a+++++++%3c%2fspan%3e%0d%0a%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b%22%3e%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fen-us%2fmicrosoft-teams%2fdownload-app%22+rel%3d%22noreferrer+noopener%22%3e%0d%0a++++++++Descargar+Teams%3c%2fa%3e+%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fmicrosoft-teams%2fjoin-a-meeting%22+rel%3d%22noreferrer+noopener%22%3eUnirse+en+la+web%3c%2fa%3e%3c%2fdiv%3e%0d%0a++++%3c%2fdiv%3e%0d%0a+%3c%2fdiv%3e%0d%0a%0d%0a%0d%0a%0d%0a%0d%0a%0d%0a%3cdiv+style%3d%22margin-bottom%3a+24px%3bmargin-top%3a+20px%3b%22%3e%0d%0a++++++++%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2faka.ms%2fJoinTeamsMeeting%22+rel%3d%22noreferrer+noopener%22%3eInf%c3%b3rmese%3c%2fa%3e++%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fmeetingOptions%2f%3forganizerId%3d5d1b9abc-a510-4135-94a2-a89b19cf14ce%26tenantId%3d9c62192c-c766-43eb-adc8-a0cbe67f8085%26threadId%3d19_meeting_MmY3OTE3MzAtYWYwYi00NjYwLWEyNTMtMDljMTJkYzkyYTNl%40thread.v2%26messageId%3d0%26language%3des%22+rel%3d%22noreferrer+noopener%22%3eOpciones+de+reuni%c3%b3n%3c%2fa%3e+%0d%0a++++++%3c%2fdiv%3e%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b+margin-bottom%3a+4px%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+12px%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e",
        "Reunión de Microsoft Teams",
        "Haga clic aquí para unirse a la reunión",
        "ID de la reunión: 262 269 681 245"),
    HINDI(
        "data:text/html,%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e%0d%0a+%0d%0a+%3cdiv+class%3d%22me-email-text%22+style%3d%22color%3a%23252424%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+lang%3d%22hi%22%3e%0d%0a++++%3cdiv+style%3d%22margin-top%3a+24px%3b+margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cspan+style%3d%22font-size%3a+24px%3b+color%3a%23252424%22%3eMicrosoft+Teams+%e0%a4%ae%e0%a5%80%e0%a4%9f%e0%a4%bf%e0%a4%82%e0%a4%97%3c%2fspan%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+20px%3b%22%3e%0d%0a++++++++%3cdiv+style%3d%22margin-top%3a+0px%3b+margin-bottom%3a+0px%3b+font-weight%3a+bold%22%3e%0d%0a++++++++++%3cspan+style%3d%22font-size%3a+14px%3b+color%3a%23252424%22%3e%e0%a4%85%e0%a4%aa%e0%a4%a8%e0%a5%87+%e0%a4%95%e0%a4%82%e0%a4%aa%e0%a5%8d%e0%a4%af%e0%a5%82%e0%a4%9f%e0%a4%b0%2c+%e0%a4%ae%e0%a5%8b%e0%a4%ac%e0%a4%be%e0%a4%87%e0%a4%b2+%e0%a4%90%e0%a4%aa+%e0%a4%af%e0%a4%be+%e0%a4%b0%e0%a5%82%e0%a4%ae+%e0%a4%a1%e0%a4%bf%e0%a4%b5%e0%a4%be%e0%a4%87%e0%a4%b8+%e0%a4%aa%e0%a4%b0+%e0%a4%b6%e0%a4%be%e0%a4%ae%e0%a4%bf%e0%a4%b2+%e0%a4%b9%e0%a5%8b%e0%a4%82%3c%2fspan%3e%0d%0a++++++++%3c%2fdiv%3e%0d%0a++++++++%3ca+class%3d%22me-email-headline%22+style%3d%22font-size%3a+14px%3bfont-family%3a%27Segoe+UI+Semibold%27%2c%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3b%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fl%2fmeetup-join%2f19%253ameeting_M2U0M2Q0MmQtYmIxZi00M2ViLWJjNzYtNDk1YjA1MWE3NWIw%2540thread.v2%2f0%3fcontext%3d%257b%2522Tid%2522%253a%25229c62192c-c766-43eb-adc8-a0cbe67f8085%2522%252c%2522Oid%2522%253a%25225d1b9abc-a510-4135-94a2-a89b19cf14ce%2522%257d%22+target%3d%22_blank%22+rel%3d%22noreferrer+noopener%22%3e%e0%a4%ae%e0%a5%80%e0%a4%9f%e0%a4%bf%e0%a4%82%e0%a4%97+%e0%a4%ae%e0%a5%87%e0%a4%82+%e0%a4%b6%e0%a4%be%e0%a4%ae%e0%a4%bf%e0%a4%b2+%e0%a4%b9%e0%a5%8b%e0%a4%a8%e0%a5%87+%e0%a4%95%e0%a5%87+%e0%a4%b2%e0%a4%bf%e0%a4%8f+%e0%a4%af%e0%a4%b9%e0%a4%be%e0%a4%81+%e0%a4%95%e0%a5%8d%e0%a4%b2%e0%a4%bf%e0%a4%95+%e0%a4%95%e0%a4%b0%e0%a5%87%e0%a4%82%3c%2fa%3e%0d%0a++++%3c%2fdiv%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a20px%3b+margin-top%3a20px%22%3e%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a4px%22%3e%0d%0a++++++++%3cspan+data-tid%3d%22meeting-code%22+style%3d%22font-size%3a+14px%3b+color%3a%23252424%3b%22%3e%0d%0a++++++++++++%e0%a4%ae%e0%a5%80%e0%a4%9f%e0%a4%bf%e0%a4%82%e0%a4%97+ID%3a+%3cspan+style%3d%22font-size%3a16px%3b+color%3a%23252424%3b%22%3e258+857+301+25%3c%2fspan%3e%0d%0a+++++++%3c%2fspan%3e%0d%0a%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b%22%3e%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fen-us%2fmicrosoft-teams%2fdownload-app%22+rel%3d%22noreferrer+noopener%22%3e%0d%0a++++++++Teams+%e0%a4%a1%e0%a4%be%e0%a4%89%e0%a4%a8%e0%a4%b2%e0%a5%8b%e0%a4%a1+%e0%a4%95%e0%a4%b0%e0%a5%87%e0%a4%82%3c%2fa%3e+%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fwww.microsoft.com%2fmicrosoft-teams%2fjoin-a-meeting%22+rel%3d%22noreferrer+noopener%22%3e%e0%a4%b5%e0%a5%87%e0%a4%ac+%e0%a4%aa%e0%a4%b0+%e0%a4%b6%e0%a4%be%e0%a4%ae%e0%a4%bf%e0%a4%b2+%e0%a4%b9%e0%a5%8b%e0%a4%82%3c%2fa%3e%3c%2fdiv%3e%0d%0a++++%3c%2fdiv%3e%0d%0a+%3c%2fdiv%3e%0d%0a++++%0d%0a++++++%0d%0a++++%0d%0a++++%0d%0a++++%0d%0a++++%3cdiv+style%3d%22margin-bottom%3a+24px%3bmargin-top%3a+20px%3b%22%3e%0d%0a++++++++%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2faka.ms%2fJoinTeamsMeeting%22+rel%3d%22noreferrer+noopener%22%3e%e0%a4%94%e0%a4%b0+%e0%a4%9c%e0%a4%be%e0%a4%a8%e0%a5%87%e0%a4%82%3c%2fa%3e++%7c+%3ca+class%3d%22me-email-link%22+style%3d%22font-size%3a+14px%3btext-decoration%3a+underline%3bcolor%3a+%236264a7%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22+target%3d%22_blank%22+href%3d%22https%3a%2f%2fteams.microsoft.com%2fmeetingOptions%2f%3forganizerId%3d5d1b9abc-a510-4135-94a2-a89b19cf14ce%26tenantId%3d9c62192c-c766-43eb-adc8-a0cbe67f8085%26threadId%3d19_meeting_M2U0M2Q0MmQtYmIxZi00M2ViLWJjNzYtNDk1YjA1MWE3NWIw%40thread.v2%26messageId%3d0%26language%3dhi%22+rel%3d%22noreferrer+noopener%22%3e%e0%a4%ae%e0%a5%80%e0%a4%9f%e0%a4%bf%e0%a4%82%e0%a4%97+%e0%a4%95%e0%a5%87+%e0%a4%b5%e0%a4%bf%e0%a4%95%e0%a4%b2%e0%a5%8d%e0%a4%aa%3c%2fa%3e+%0d%0a++++++%3c%2fdiv%3e%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+14px%3b+margin-bottom%3a+4px%3bfont-family%3a%27Segoe+UI%27%2c%27Helvetica+Neue%27%2cHelvetica%2cArial%2csans-serif%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22font-size%3a+12px%3b%22%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%0d%0a%3c%2fdiv%3e%0d%0a%3cdiv+style%3d%22width%3a100%25%3bheight%3a+20px%3b%22%3e%0d%0a++++%3cspan+style%3d%22white-space%3anowrap%3bcolor%3a%235F5F5F%3bopacity%3a.36%3b%22%3e________________________________________________________________________________%3c%2fspan%3e%0d%0a%3c%2fdiv%3e\n",
        "Microsoft Teams मीटिंग",
        "मीटिंग में शामिल होने के लिए यहाँ क्लिक करें",
        "मीटिंग ID: 258 857 301 25");

    public final String contentDataUrl;
    public final String headingText;
    public final String linkText;
    public final String meetingIdText;
  }
}
