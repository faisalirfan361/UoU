import React, { FC, useMemo } from "react";
import CardContent from "@material-ui/core/CardContent";
import Divider from "@material-ui/core/Divider";
import Card from "@material-ui/core/Card";

import IProps from "./UserPerformanceCardTypes";
import Style from "./style";
import StyledAvatar from "../../StyledAvatar";
import PerformanceCardHeader from "../Components/PerformanceCardHeader";
import KpiPerformanceBars from "../Components/KpiPerformanceBars";
import CardCTA from "../Components/CardCTA";
import { getUserStatus, useStatusColor } from "../../../utils/kpiStatusHelper";
import formatDistance from "date-fns/formatDistance";
import startOfWeek from "date-fns/startOfWeek";
import startOfMonth from "date-fns/startOfMonth";
import { endOfWeek, endOfMonth, endOfDay, startOfDay, parse } from "date-fns";
import startOfYear from "date-fns/startOfYear";
import format from "date-fns/format";

/**
 *
 * @param recurrence is the duration of the goal
 * @param dateFormate is the formate of the goal
 * @returns
 */
const getStartDate = (recurrence: string, dateFormate: string): string => {
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
const getEndDate = (recurrence: string, dateFormate: string): string => {
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
const getWeekStartDate = (dateFormate: string): string => {
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
const formateKPIDate = (date: string, dateFormate: string): string => {
  return format(endOfDay(new Date(date)), dateFormate);
};

/**
 * this fucntion is used to check if the date is between these two dates
 * @param from is starting date of the goal
 * @param to is ending date of the goal
 * @param check is date to compare against
 * @returns boolean value
 */
const dateCheck = (from: string, to: string, check: string) => {
  let fDate, lDate, cDate;
  fDate = Date.parse(from);
  lDate = Date.parse(to);
  cDate = Date.parse(check);
  if ((cDate <= lDate && cDate >= fDate) || from == check) return true;
  return false;
};

const getKpiSettings = (goals: any, kpiSettings: any) => {
  if (!goals) return [];

  return goals.map((goal: any) => {
    return {
      code: goal.attributes.goalName,
      title: goal.attributes.goalName,
      goal: goal.attributes.goalValue,
      obtained: getObtained(goal, kpiSettings),
    };
  });
};

const getObtained = (goal: any, kpiSettings: any) => {
  if (!kpiSettings.kpisState) return 0;

  const newObj = Object.entries(kpiSettings.kpisState);
  const newDate: any = newObj.find(
    (obj: any) => obj[0] === goal.attributes.indicator
  )?.[1];
  let score: number = 0;
  const dateEnd = getEndDate(goal.attributes.metricDuration, "yyyy-MM-dd");
  const dateStart = getWeekStartDate("yyyy-MM-dd");
  if (newDate) {
    Object.keys(newDate)
      .sort(function (a, b) {
        return a
          .split("_")
          .reverse()
          .join("")
          .localeCompare(b.split("_").reverse().join(""));
      })
      .map((item, ind) => {
        const date = item.replace(/_/g, "-");
        const formattedDate = formateKPIDate(date, "yyyy-MM-dd");
        if (dateCheck(dateStart, dateEnd, formattedDate)) {
          score += newDate[item];
        }
      }); //sorting dates and its coresponding values
    return score;
  } else {
    return 0;
  }
};

const UserPerformanceCard: FC<IProps> = ({
  user,
  userDetailsFunc,
  kpiSettings,
}) => {
  const { getStatusColor } = useStatusColor();

  const kpisSettings = useMemo(
    () => getKpiSettings(kpiSettings, user.performance),
    [user.performance]
  );

  const status = getUserStatus(kpisSettings);
  const statusColor = getStatusColor(status);
  const overrideProps = { statusColor };
  const classes = Style(overrideProps)();

  return (
    <Card classes={{ root: classes.root }} elevation={2}>
      <CardContent>
        <PerformanceCardHeader
          status={status}
          statusColor={statusColor}
          mainText={`${user.firstName} ${user.lastName}`}
          secondaryText={`${user.role.name}`}
        >
          <StyledAvatar src={user.profileImg} className={classes.userAvatar} />
        </PerformanceCardHeader>

        <Divider />

        <KpiPerformanceBars kpiSettings={kpisSettings} />

        <CardCTA actionFunc={userDetailsFunc} ctaText={"View Agent"} />
      </CardContent>
    </Card>
  );
};

export default UserPerformanceCard;
