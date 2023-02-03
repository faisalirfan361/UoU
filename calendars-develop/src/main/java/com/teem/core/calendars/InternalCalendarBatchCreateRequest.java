package com.UoU.core.calendars;

import com.UoU.core.OrgId;
import com.UoU.core.validation.annotations.Custom;
import com.UoU.core.validation.annotations.NotZero;
import com.UoU.core.validation.annotations.StringContains;
import com.UoU.core.validation.annotations.TimeZone;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.val;

/**
 * Request to create a batch of internal calendars based on a naming pattern.
 */
@Custom(use = InternalCalendarBatchCreateRequest.Validator.class)
public record InternalCalendarBatchCreateRequest(
    @NotNull @Valid OrgId orgId,
    @NotNull @StringContains(NUMBER_TOKEN) @Size(max = CalendarConstraints.NAME_MAX)
    String namePattern,
    @NotNull @TimeZone String timezone,
    @NotNull Integer start,
    @NotNull Integer end,
    @NotNull @NotZero Integer increment,
    boolean isDryRun
) {

  /**
   * Token that will be replaced in the name pattern to create calendar names. Note that the number
   * itself can be up to 3 digits, so if we also make the token 3 chars then replacing the token
   * with the number won't alter the total name length, so validation max won't need to change.
   */
  public static final String NUMBER_TOKEN = "{n}";

  private static final String DRY_RUN_NAME_SUFFIX = " (DRY RUN)";

  /**
   * Formats a name from the namePattern, given the batch item number.
   */
  public String formatName(int number) {
    val name = namePattern.replace(NUMBER_TOKEN, String.valueOf(number));
    return isDryRun ? name + DRY_RUN_NAME_SUFFIX : name;
  }

  public static class Validator
      implements Custom.Validator<InternalCalendarBatchCreateRequest> {
    @Override
    public boolean isValid(
        InternalCalendarBatchCreateRequest value, ConstraintValidatorContext context) {

      if (value.start() != null && value.end() != null) {
        // For negative increment, ensure end <= start.
        // For positive increment, ensure end >= start.
        if (value.increment() < 0 && value.end() > value.start()) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate("must be less than or equal to start")
              .addPropertyNode("end")
              .addConstraintViolation();
          return false;
        } else if (value.increment() > 0 && value.end() < value.start()) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate("must be greater than or equal to start")
              .addPropertyNode("end")
              .addConstraintViolation();
          return false;
        }
      }

      return true;
    }
  }
}
