import { endOfDay, endOfMonth, endOfWeek, format, startOfDay, startOfMonth, startOfWeek, startOfYear } from "date-fns";
//import format from "date-fns/format";
export const isDayAfter = (endDate: Date, startDate: Date) => {
  return endDate > startDate;
};

/**
 *
 * @param recurrence is the duration of the goal
 * @param dateFormate is the formate of the goal
 * @returns
 */
 export const getStartDate = (recurrence: string, dateFormate: string): string => {
  const now = new Date();
  if (recurrence === "Daily") {
    return dateFormate
      ? format(startOfDay(now), dateFormate)
      : `${startOfDay(now)}`;
  } else if (recurrence === "Weekly") {
    return dateFormate
      ? format(startOfWeek(now), dateFormate)
      : `${startOfWeek(now)}`;
  } else if (recurrence === "Monthly") {
    return dateFormate
      ? format(startOfMonth(now), dateFormate)
      : `${startOfMonth(now)}`;
  }
  return dateFormate
    ? format(startOfYear(now), dateFormate)
    : `${startOfYear(now)}`;
 };

 /**
 *
 * @param recurrence is the duration of the goal
 * @param dateFormate is the formate of the goal
 * @returns
 */
export const getEndDate = (recurrence: string, dateFormate: string): string => {
  const now = new Date();
  if (recurrence === "Daily") {
    return dateFormate
      ? format(endOfDay(now), dateFormate)
      : `${endOfDay(now)}`;
  } else if (recurrence === "Weekly") {
    return dateFormate
      ? format(endOfWeek(now), dateFormate)
      : `${endOfWeek(now)}`;
  } else if (recurrence === "Monthly") {
    return dateFormate
      ? format(endOfMonth(now), dateFormate)
      : `${endOfMonth(now)}`;
  }
  return dateFormate
    ? format(startOfYear(now), dateFormate)
    : `${startOfYear(now)}`;
};

/**
 *
 * @param dateFormate is the formate of the date
 * @returns this return the date from last week
 */
export const getWeekStartDate = (dateFormate: string): string => {
  let now = new Date();
  const result = now.setDate(now.getDate() - 6);
  return dateFormate
    ? format(startOfDay(result), dateFormate)
    : `${startOfDay(result)}`;
};

/**
 *
 * @param dateFormate is the formate of the date
 * @param date date of kpi result
 * @returns this return the date from last week
 */
export const formateKPIDate = (date: string, dateFormate: string): string => {
  return format(endOfDay(new Date(date)), dateFormate);
};

/**
   * this fucntion is used to check if the date is between these two dates
   * @param from is starting date of the goal
   * @param to is ending date of the goal
   * @param check is date to compare against
   * @returns boolean value
   */
 export const dateCheck = (from: string, to: string, check: string) => {
  let fDate, lDate, cDate;
  fDate = Date.parse(from);
  lDate = Date.parse(to);
  cDate = Date.parse(check);
  if ((cDate <= lDate && cDate >= fDate) || from == check) return true;
  return false;
};