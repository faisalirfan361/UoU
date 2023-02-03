package com.UoU.core.events;

import com.UoU.core.validation.ValidatorChecker;
import com.UoU.core.validation.annotations.Custom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;
import javax.validation.ConstraintValidatorContext;

@Custom(use = When.Validator.class)
public interface When {

  Type type();

  /**
   * An exact UTC timespan that has been previously calculated with the calendar timezone.
   *
   * <p>This value may or may not be set, and if set, it should be equal to the result of calling
   * {@link #toUtcTimeSpan(Supplier)} with the calendar's timezone. This just allows storing the
   * timespan with the object so it doesn't have to be recalculated over and over again, which
   * requires knowing the calendar timezone. This value should be set when reading events from the
   * db, but it may not be set during event creates and updates because the effective points in time
   * may still need to be calculated based on the calendar timezone.
   */
  Optional<com.UoU.core.TimeSpan> effectiveUtcTimeSpan();

  /**
   * Creates an exact UTC timespan by interpreting the time period with the given timezone.
   *
   * <p>If the object already represents an exact point in time, no interpretation is required.
   * However, for all-day events, which don't represent a specific time, we sometimes need to
   * interpret exact points in time for things like calculating availability.
   *
   * <p>The zoneSupplier is lazy and will only be called when a zone is needed for the result.
   */
  com.UoU.core.TimeSpan toUtcTimeSpan(Supplier<ZoneId> zoneSupplier);

  /**
   * Whether the event is all-day, which means {@link #toAllDayDateSpan()} will also return a value.
   */
  default boolean isAllDay() {
    return toAllDayDateSpan().isPresent();
  }

  /**
   * Span of dates for all-day events, else empty.
   */
  Optional<DateSpan> toAllDayDateSpan();

  String startPropertyName();

  enum Type {
    TIMESPAN,
    DATESPAN,
    DATE,
  }

  /**
   * Span of time that represents specific points in time, with seconds precision to match Nylas.
   *
   * <p>Rules:
   * - startTime and endTime are @NotNull, but validation is handled at class-level.
   * - endTime must be after startTime
   */
  record TimeSpan(
      Instant startTime,
      Instant endTime)
      implements When {

    public TimeSpan {
      // Truncate to seconds precision to match Nylas:
      // TODO: Add tests for this, and check for other places where we might need to do this.
      // We want to make sure that we match Nylas' level of precision so the times we store match
      // their event times exactly. For some reason, some tests started failing on CodeFresh
      // because of time precision even though they work locally. I don't quite understand what
      // was happening since the JDK and environment is the same. But it needs fixing anyway.
      startTime = startTime == null ? null : startTime.truncatedTo(ChronoUnit.SECONDS);
      endTime = endTime == null ? null : endTime.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public Type type() {
      return Type.TIMESPAN;
    }


    @Override
    public Optional<com.UoU.core.TimeSpan> effectiveUtcTimeSpan() {
      return Optional.of(new com.UoU.core.TimeSpan(startTime, endTime));
    }

    @Override
    public com.UoU.core.TimeSpan toUtcTimeSpan(Supplier<ZoneId> zoneSupplier) {
      return new com.UoU.core.TimeSpan(startTime, endTime);
    }

    @Override
    public Optional<DateSpan> toAllDayDateSpan() {
      // This type can't represent all-day events, so return empty.
      return Optional.empty();
    }

    @Override
    public String startPropertyName() {
      return "startTime";
    }
  }

  /**
   * Span of full days without time or timezones.
   *
   * <p>Rules:
   * - startDate and endDate are @NotNull, but validation is handled at class-level.
   * - endDate must be after startDate (else use Date instead for single a day).
   */
  record DateSpan(
      LocalDate startDate,
      LocalDate endDate,
      Optional<com.UoU.core.TimeSpan> effectiveUtcTimeSpan)
      implements When {

    public DateSpan(LocalDate startDate, LocalDate endDate) {
      this(startDate, endDate, Optional.empty());
    }

    public DateSpan(
        LocalDate startDate,
        LocalDate endDate,
        com.UoU.core.TimeSpan effectiveUtcTimeSpan) {
      this(startDate, endDate, Optional.ofNullable(effectiveUtcTimeSpan));
    }

    public DateSpan {
      effectiveUtcTimeSpan = effectiveUtcTimeSpan != null ? effectiveUtcTimeSpan : Optional.empty();
    }

    @Override
    public Type type() {
      return Type.DATESPAN;
    }

    @Override
    public com.UoU.core.TimeSpan toUtcTimeSpan(Supplier<ZoneId> zoneSupplier) {
      var zone = zoneSupplier.get();
      return new com.UoU.core.TimeSpan(
          startDate.atStartOfDay(zone).toInstant(),
          endDate.plusDays(1).atStartOfDay(zone).toInstant()); // Add +1 day to make end exclusive.
    }

    @Override
    public Optional<DateSpan> toAllDayDateSpan() {
      return Optional.of(new DateSpan(startDate, endDate));
    }

    @Override
    public String startPropertyName() {
      return "startDate";
    }
  }

  /**
   * Date that represents a full day without time or timezone.
   *
   * <p>Rules:
   * - date is @NotNull, but validation is handled at class-level.
   */
  record Date(
      LocalDate date,
      Optional<com.UoU.core.TimeSpan> effectiveUtcTimeSpan)
      implements When {

    public Date(LocalDate date) {
      this(date, Optional.empty());
    }

    public Date(LocalDate date, com.UoU.core.TimeSpan effectiveUtcTimeSpan) {
      this(date, Optional.ofNullable(effectiveUtcTimeSpan));
    }

    public Date {
      effectiveUtcTimeSpan = effectiveUtcTimeSpan != null ? effectiveUtcTimeSpan : Optional.empty();
    }

    @Override
    public Type type() {
      return Type.DATE;
    }

    @Override
    public com.UoU.core.TimeSpan toUtcTimeSpan(Supplier<ZoneId> zoneSupplier) {
      var zone = zoneSupplier.get();
      return new com.UoU.core.TimeSpan(
          date.atStartOfDay(zone).toInstant(),
          date.plusDays(1).atStartOfDay(zone).toInstant()); // Add +1 day to make end exclusive.
    }

    @Override
    public Optional<DateSpan> toAllDayDateSpan() {
      return Optional.of(new DateSpan(date, date));
    }

    @Override
    public String startPropertyName() {
      return "date";
    }
  }

  class Validator implements Custom.Validator<When> {
    private static final ValidatorChecker<TimeSpan> TIME_SPAN_CHECKER =
        new ValidatorChecker<TimeSpan>()
            .add((val, ctx) -> val.startTime() != null,
                "startTime",
                "Start must not be null.")
            .add((val, ctx) -> val.endTime() != null
                    && (val.startTime() == null || val.endTime().isAfter(val.startTime())),
                "endTime",
                "End must not be null and must be after start.");

    private static final ValidatorChecker<DateSpan> DATE_SPAN_CHECKER =
        new ValidatorChecker<DateSpan>()
            .add((val, ctx) -> val.startDate() != null,
                "startDate",
                "Start must not be null.")
            .add((val, ctx) -> val.endDate() != null
                    && (val.startDate() == null || val.endDate().isAfter(val.startDate())),
                "endDate",
                "End must not be null and must be after start.");

    private static final ValidatorChecker<Date> DATE_CHECKER =
        new ValidatorChecker<Date>()
            .add((val, ctx) -> val.date() != null,
                "date",
                "Date must not be null.");

    @Override
    public boolean isValid(When when, ConstraintValidatorContext context) {
      if (when instanceof TimeSpan) {
        return TIME_SPAN_CHECKER.isValid((TimeSpan) when, context);
      } else if (when instanceof DateSpan) {
        return DATE_SPAN_CHECKER.isValid((DateSpan) when, context);
      } else if (when instanceof Date) {
        return DATE_CHECKER.isValid((Date) when, context);
      }

      return false;
    }
  }
}
