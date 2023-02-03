import { isInteger } from "lodash";
const locale = "en-US";

export function humanReadableDecimal(num?: number | string): string {
  if (!!num) return Intl.NumberFormat(locale).format(num as number);
  return "0";
}

export function humanReadableTime(date: Date): string {
  return date.toLocaleTimeString(locale, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function humanReadableDate(date: Date): string {
  return date.toLocaleDateString(locale, { dateStyle: "short" });
}

export function humanReadableTimeStamp(date: Date): string {
  return date.toLocaleString(locale, {
    timeStyle: "short",
    dateStyle: "short",
  });
}
