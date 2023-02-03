package com.UoU.core.events;

import com.UoU.core.validation.ValidatorChecker;
import com.UoU.core.validation.annotations.Custom;
import com.UoU.core.validation.annotations.TimeZone;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

/**
 * Encapsulates event recurrence info, including info about a series master or an instance.
 *
 * <p>An event may be a series master (`isMaster()`) or an instance (`isInstance()`), but it cannot
 * be both. An instance may be an override (deviates from the master) or not. Non-override instances
 * can be generated automatically from the master, while overrides cannot because they represent
 * changes that are not captured by the master itself. See the Master and Instance nested classes.
 */
@Getter
public class Recurrence {
  private static final Recurrence NONE_INSTANCE = new Recurrence();

  @Valid private final Master master;
  @Valid private final Instance instance;

  private Recurrence() {
    this.master = null;
    this.instance = null;
  }

  private Recurrence(@NonNull Master master) {
    this.master = master;
    this.instance = null;
  }

  private Recurrence(@NonNull Instance instance) {
    this.master = null;
    this.instance = instance;
  }

  public boolean isMaster() {
    return master != null;
  }

  public Optional<Master> withMaster() {
    return Optional.ofNullable(master);
  }

  public boolean isInstance() {
    return instance != null;
  }

  public boolean isInstanceThat(Function<Instance, Boolean> condition) {
    return withInstance().filter(condition::apply).isPresent();
  }

  public Optional<Instance> withInstance() {
    return Optional.ofNullable(instance);
  }

  public static Recurrence none() {
    return NONE_INSTANCE;
  }

  public static Recurrence master(Recurrence.Master master) {
    return new Recurrence(master);
  }

  public static Recurrence master(List<String> rrule, String timezone) {
    return new Recurrence(new Master(rrule, timezone));
  }

  public static Recurrence master(
      List<String> rrule, String timezone, Master.ValidationContext validationContext) {
    return new Recurrence(new Master(rrule, timezone, validationContext));
  }

  public static Recurrence instance(EventId masterId, boolean isOverride) {
    return new Recurrence(new Instance(masterId, isOverride));
  }

  /**
   * Info about a recurrence master that defines the schedule for the recurrence series.
   *
   * <p>Note that we limit the RRULE spec and store RRULEs the same way as Nylas so that we can sync
   * to Nylas and avoid incompatible features and translation issues. See the
   * {@link Master.Validator} for what's considered a valid RRULE.
   *
   * <p>Use the ctor with ValidationContext for full validation in the context of an event.
   */
  @Custom(use = Master.Validator.class)
  public record Master(
      @NotEmpty List<String> rrule,
      @NotNull @TimeZone String timezone,
      ValidationContext validationContext) {

    public Master(List<String> rrule, String timezone) {
      this(rrule, timezone, null);
    }

    public Master {
      // Filter out blank rrule values so @NotEmpty will require at least one value.
      rrule = rrule == null ? List.of() : rrule.stream().filter(x -> !x.isBlank()).toList();
    }

    /**
     * Returns the RRULE expression, which is always the first item in the rrule list.
     */
    public Optional<String> rruleExpression() {
      return Optional.of(rrule)
          .filter(x -> x.size() >= 1)
          .map(x -> x.get(0));
    }

    /**
     * Returns the EXDATE expression, which can optionally be the second item in the rrule list.
     */
    public Optional<String> exdateExpression() {
      return Optional.of(rrule)
          .filter(x -> x.size() >= 2)
          .map(x -> x.get(1));
    }

    /**
     * Returns whether the data properties are all equal, ignoring non-data properties.
     *
     * <p>Unlike {@link #equals(Object)}, this ignores validationContext and only compares the
     * properties that represent event data.
     */
    public boolean dataEquals(Master obj) {
      return obj != null
          && Objects.equals(rrule, obj.rrule)
          && Objects.equals(timezone, obj.timezone);
    }

    public record ValidationContext(boolean isAllDay) {
    }

    /**
     * Does some extra validation on the master to prevent invalid RRULEs.
     */
    public static class Validator implements Custom.Validator<Master> {
      private static final String RRULE_EXPR_PREFIX = "RRULE:";
      private static final String EXDATE_EXPR_PREFIX = "EXDATE:";
      private static final String EXDATE_EXPR_SEPARATOR = ",";
      private static final Pattern EXDATE_EXPR_DATE_PATTERN = Pattern.compile(
          "^\\d{8}T\\d{6}Z?$");

      // DO-LATER: We may want to do more advanced validation or somehow handle RRULEs and EXDATEs
      // that fail on sync to Nylas because of data-specific reasons, like an EXDATE not being
      // an actual instance. This can get pretty complex because it goes beyond the RRULE spec.
      private static final ValidatorChecker<Master> CHECKER = new ValidatorChecker<Master>()
          .add((val, ctx) -> val.rrule().size() == 1 || val.rrule().size() == 2,
              "rrule",
              "Rrule array must contain one RRULE item and then optionally one EXDATE item.")
          .add((val, ctx) -> val
                  .rruleExpression()
                  .map(x -> isValidRruleExpression(x, val.validationContext()))
                  .orElse(true),
              "rrule",
              "Rrule item 1 must be a valid RRULE starting with '" + RRULE_EXPR_PREFIX + "'.")
          .add((val, ctx) -> val
                  .exdateExpression()
                  .map(Validator::isValidExdateExpression)
                  .orElse(true),
              "rrule",
              "Rrule item 2, if provided, must be a valid EXDATE expression starting with '"
                  + EXDATE_EXPR_PREFIX + "'.");

      @Override
      public boolean isValid(Master value, ConstraintValidatorContext context) {
        return CHECKER.isValid(value, context);
      }

      /**
       * Validates that RRULE matches: "RRULE:[valid-rrule-for-rfc-5545]".
       *
       * <p>Valid example: RRULE:FREQ=DAILY;UNTIL=20220511T200000
       */
      private static boolean isValidRruleExpression(String expr, ValidationContext context) {
        if (expr == null || !expr.startsWith(RRULE_EXPR_PREFIX)) {
          return false; // use default error message
        }

        val rruleString = expr.substring(RRULE_EXPR_PREFIX.length());
        if (rruleString.isBlank()) {
          return false; // use default error message
        }

        // Nylas does not allow trailing semicolons, but lib-recur does, so check separately:
        if (expr.endsWith(";")) {
          return ValidatorChecker.failIsValidCheck("Rrule cannot end with a semicolon.");
        }

        RecurrenceRule rrule;
        try {
          // Use lib-recur to parse RRULE, and if exception, it's invalid.
          // Use RFC 5545 because it obsoletes RFC 2445, though they are not much different.
          rrule = new RecurrenceRule(rruleString, RecurrenceRule.RfcMode.RFC5545_STRICT);
        } catch (InvalidRecurrenceRuleException ex) {
          // lib-recur messages are helpful to know why the rrule is invalid, so provide to user:
          return ValidatorChecker.failIsValidCheck(
              "Rrule is invalid according to RFC 5545. " + ex.getMessage());
        }

        // If validation context is provided, validate for the specific context, else skip.
        if (context != null) {
          // If the event is all-day, require a date only (no time or timezone).
          // If not all day, require a datetime with UTC suffix of Z.
          // This is part of the spec: https://www.rfc-editor.org/rfc/rfc5545#section-3.3.10
          // lib-recur parses all-day UNTIL as "isAllDay" and anything without Z as "isFloating", so
          // we can use the parsed rule to determine the above conditions.
          val until = rrule.getUntil();
          if (until != null
              && context.isAllDay()
              && !until.isAllDay()) {
            return ValidatorChecker.failIsValidCheck(
                "Rrule UNTIL must be a date without time because the event is all-day. "
                    + "For example: RRULE:FREQ=DAILY;UNTIL=20220815");
          } else if (until != null
              && !context.isAllDay()
              && (until.isAllDay() || until.isFloating())) {
            return ValidatorChecker.failIsValidCheck(
                "Rrule UNTIL must be a UTC datetime with 'Z' suffix because the event start is "
                    + "an exact point in time (as opposed to an all-day event). "
                    + "For example: RRULE:FREQ=DAILY;UNTIL=20220815T140000Z");
          }
        }

        return true;
      }

      /**
       * Validates that EXDATE matches: "EXDATE:[iso-datetime],[iso-datetime]".
       *
       * <p>Valid example: EXDATE:20220512T200000Z,20220513T200000Z
       */
      private static boolean isValidExdateExpression(String expr) {
        if (expr == null || !expr.startsWith(EXDATE_EXPR_PREFIX)) {
          return false;
        }

        return Arrays
            .stream(expr.substring(EXDATE_EXPR_PREFIX.length()).split(EXDATE_EXPR_SEPARATOR))
            .allMatch(date -> EXDATE_EXPR_DATE_PATTERN.matcher(date.trim()).matches());
      }
    }
  }

  /**
   * Info about a recurrence instance, which is one particular event within a series.
   *
   * <p>An instance either fits the master schedule exactly (isOverride=false) or is an override
   * that represents a deviation from the master schedule (isOverride=true). Non-override instances
   * can be auto-generated from the schedule where needed, while overrides always have to be taken
   * into account when expanding the series.
   */
  public record Instance(
      @NotNull @Valid EventId masterId,
      boolean isOverride
  ) {
    public boolean isNotOverride() {
      return !isOverride;
    }

    public boolean isInSeries(EventId seriesMasterId) {
      return masterId.equals(seriesMasterId);
    }
  }
}
