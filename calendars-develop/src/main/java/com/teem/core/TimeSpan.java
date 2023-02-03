package com.UoU.core;

import com.UoU.core.validation.ValidatorChecker;
import com.UoU.core.validation.annotations.Custom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.tuple.Pair;

/**
 * General purpose representation of a time span between two instants.
 *
 * @param start The start of the time span, inclusive.
 * @param end The end of the time span, exclusive.
 * @param maxDuration The max duration for validation (does nothing outside validation). This can
 *                    be set to Duration.Zero to allow any duration.
 */
@Custom(use = TimeSpan.Validator.class)
public record TimeSpan(
    @NotNull Instant start,
    @NotNull Instant end,
    @NotNull Duration maxDuration
) {
  public TimeSpan(@NotNull Instant start, @NotNull Instant end) {
    this(start, end, Duration.ZERO);
  }

  public TimeSpan(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end) {
    this(start.toInstant(), end.toInstant(), Duration.ZERO);
  }

  /**
   * Gets the start as an OffsetDateTime at UTC offset.
   */
  public OffsetDateTime startAtUtcOffset() {
    return start.atOffset(ZoneOffset.UTC);
  }

  /**
   * Gets the end as an OffsetDateTime at UTC offset.
   */
  public OffsetDateTime endAtUtcOffset() {
    return end.atOffset(ZoneOffset.UTC);
  }

  /**
   * Actual duration between start and end.
   */
  public Duration duration() {
    return Duration.between(start, end);
  }

  /**
   * Creates a new TimeSpan with the max duration set for validation.
   */
  public TimeSpan withMaxDuration(Duration max) {
    return new TimeSpan(start, end, max);
  }

  /**
   * Gets a user-friendly description of the max duration.
   */
  public String maxDurationDescription() {
    if (maxDuration == Duration.ZERO) {
      return "none";
    }

    return Stream
        .of(
            Pair.of(maxDuration.toDaysPart(), "day"),
            Pair.of(maxDuration.toHoursPart(), "hour"),
            Pair.of(maxDuration.toMinutesPart(), "minute"),
            Pair.of(maxDuration.toSecondsPart(), "second"))
        .filter(x -> x.getLeft().longValue() > 0)
        .map(x -> x.getLeft() + " " + x.getRight() + (x.getLeft().longValue() > 1 ? "s" : ""))
        .collect(Collectors.joining(", "));
  }

  /**
   * Checks whether the passed instant is within the timespan.
   *
   * <p>Note that the timespan start is inclusive, while the end is exclusive.
   */
  public boolean contains(Instant instant) {
    return start.compareTo(instant) <= 0 && end.isAfter(instant);
  }

  /**
   * Checks whether the passed timeSpan is entirely within this timespan.
   */
  public boolean contains(TimeSpan timeSpan) {
    return start.compareTo(timeSpan.start()) <= 0 && end.compareTo(timeSpan.end()) >= 0;
  }

  /**
   * Checks whether the timespan ends before the passed instant (not including it).
   *
   * <p>Note that the timespan end is exclusive, so the timespan will be before if end <= instant.
   */
  public boolean isBefore(Instant instant) {
    return end.compareTo(instant) <= 0;
  }

  public static class Validator implements Custom.Validator<TimeSpan> {
    private static final ValidatorChecker<TimeSpan> CHECKER = new ValidatorChecker<TimeSpan>()
        .add((val, ctx) -> val.start() != null,
            "start",
            "Start must not be null.")
        .add((val, ctx) -> val.end() != null
                && (val.start() == null || val.end().isAfter(val.start())),
            "end",
            "End must not be null and must be after start.")
        .add((val, ctx) -> val.end() == null
                || val.maxDuration() == Duration.ZERO
                || val.duration().toSeconds() <= val.maxDuration().toSeconds(),
            "end",
            val -> "End cannot be more than " + val.maxDurationDescription() + " after start.");

    @Override
    public boolean isValid(TimeSpan value, ConstraintValidatorContext context) {
      return CHECKER.isValid(value, context);
    }
  }
}
