package com.UoU.app.v1.dtos;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WhenDto.TimeSpan.class, name = "timespan"),
    @JsonSubTypes.Type(value = WhenDto.DateSpan.class, name = "datespan"),
    @JsonSubTypes.Type(value = WhenDto.Date.class, name = "date")
})
@Schema(name = "When")
public interface WhenDto {

  String EFFECTIVE_START_DESC = """
      UTC timestamp that represents the all-day event start in the calendar timezone, which is only
      used when an exact point in time is needed (such as for availability endpoints).""";

  String EFFECTIVE_END_DESC = """
      UTC timestamp that represents the all-day event end in the calendar timezone, which is only
      used when an exact point in time is needed (such as for availability endpoints).""";

  @JsonProperty
  @Schema(required = true)
  Type type();

  enum Type {
    @JsonProperty("timespan") TIMESPAN,
    @JsonProperty("datespan") DATESPAN,
    @JsonProperty("date") DATE,
  }

  /**
   * Span of time that represents specific points in time.
   */
  @Schema(
      name = "WhenTimeSpan",
      example = "{\"type\": \"timespan\", "
          + "\"startTime\": \"2022-02-24T10:00:00Z\", "
          + "\"endTime\": \"2022-02-24T10:30:00Z\"}")
  record TimeSpan(
      @Schema(required = true)
      Instant startTime,

      @Schema(required = true)
      Instant endTime)
      implements WhenDto {

    @Override
    public Type type() {
      return Type.TIMESPAN;
    }
  }

  /**
   * Span of full days without time or timezones.
   */
  @Schema(
      name = "WhenDateSpan",
      example = "{\"type\": \"datespan\", "
          + "\"startDate\": \"2022-02-24\", "
          + "\"endDate\": \"2022-02-25\"}")
  record DateSpan(
      @Schema(required = true)
      LocalDate startDate,

      @Schema(required = true)
      LocalDate endDate,

      @Schema(required = true, accessMode = READ_ONLY, description = EFFECTIVE_START_DESC)
      Instant effectiveUtcStartTime,

      @Schema(required = true, accessMode = READ_ONLY, description = EFFECTIVE_END_DESC)
      Instant effectiveUtcEndTime)
      implements WhenDto {

    public DateSpan(LocalDate startDate, LocalDate endDate) {
      this(startDate, endDate, null, null);
    }

    @Override
    public Type type() {
      return Type.DATESPAN;
    }
  }

  /**
   * Date that represents a full day without time or timezone.
   */
  @Schema(
      name = "WhenDate",
      example = "{\"type\": \"date\", \"date\": \"2022-02-24\"}")
  record Date(
      @Schema(required = true)
      LocalDate date,

      @Schema(required = true, accessMode = READ_ONLY, description = EFFECTIVE_START_DESC)
      Instant effectiveUtcStartTime,

      @Schema(required = true, accessMode = READ_ONLY, description = EFFECTIVE_END_DESC)
      Instant effectiveUtcEndTime)
      implements WhenDto {

    public Date(LocalDate date) {
      this(date, null, null);
    }

    @Override
    public Type type() {
      return Type.DATE;
    }
  }
}
