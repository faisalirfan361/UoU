import React, { useState, useEffect } from "react";
import {
  Paper,
  Grid,
  Container,
  Typography,
  Tooltip,
  Divider,
} from "@material-ui/core";
import { darken } from "@material-ui/core/styles";
import CircleIcon from "@material-ui/icons/FiberManualRecord";
import CustSatIcon from "@material-ui/icons/SentimentSatisfiedAlt";
import Reward from "react-rewards";
import TrackVisibility from "react-on-screen";
import useStyles, {
  MeterColor,
  AvatarStyles,
  getGoalIconStyles,
} from "./styles";
import GoalMeterProps, { MeterStatus } from "./types";
import ChartData from "./chartData";
import StyledAvatar from "../StyledAvatar";
import { PerformanceLineChart } from "components/PerformanceLineChart";
import { PerformanceLineTrend } from "components/PerformanceLineTrend";
import formatDistance from "date-fns/formatDistance";
import startOfWeek from "date-fns/startOfWeek";
import startOfMonth from "date-fns/startOfMonth";
import { endOfWeek, endOfMonth, endOfDay, startOfDay, parse } from "date-fns";
import startOfYear from "date-fns/startOfYear";
import format from "date-fns/format";
import { split, sortBy } from "lodash";
import moment from "moment";
/**
 *
 * @param status goal status
 * @returns goal status text
 */
const getStatusTitle = (status: MeterStatus): string => {
  return (
    {
      [MeterStatus.BEHIND]: "Behind",
      [MeterStatus.MET]: "Goal Met",
      [MeterStatus.ON_TRACK]: "On Track",
    }[status] || ""
  );
};

/**
 *
 * @param status goal status
 * @returns color of the goal status
 */
const getStatusColor = (status: MeterStatus): string => {
  return (
    {
      [MeterStatus.BEHIND]: MeterColor.RED,
      [MeterStatus.MET]: MeterColor.GREEN,
      [MeterStatus.ON_TRACK]: MeterColor.YELLOW,
    }[status] || "white"
  );
};

/**
 *
 * @param meterPosition agent current point of the goal
 * @param meterGoal points need to meet the goal
 * @returns
 */
const getLinePosition = (
  meterPosition: number,
  meterGoal: number,
  flip?: boolean
): string => {
  if (flip) {
    if (meterPosition <= meterGoal) {
      return `calc(80% - 2px)`;
    } else {
      const percentage = (meterGoal / meterPosition) * 80;
      if (percentage < 0) {
        return `calc(0px)`;
      } else if (percentage > 80) {
        return `calc(80% - 2px)`;
      }
      return `calc(${percentage}% - 2px)`;
    }
  }

  if (meterPosition <= meterGoal) {
    const percentage = (meterPosition / meterGoal) * 80;
    return `calc(${percentage}% - 2px)`;
  }
  return `calc(80% - 2px)`;
};

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
  const dt = new Date(new Date(date));
  const dtDateOnly = new Date(
    dt.valueOf() + dt.getTimezoneOffset() * 60 * 1000
  );
  return format(dtDateOnly, dateFormate);
};

export const GoalsMeter: React.FC<GoalMeterProps> = (props) => {
  const styles = useStyles();
  const [meterPosition, setMeterPosition] = useState(0);
  const [status, setStatus] = useState(0);
  const statusTitle = getStatusTitle(status);
  const statusColor = getStatusColor(status);
  const linePosition: string = getLinePosition(
    meterPosition!,
    props.meterGoal!,
    props.flip
  );
  const [duration, setDuration] = useState("");
  const [remaining, setRemaining] = useState("");
  const startDate = new Date(getStartDate(props.meterDuration, ""));
  const endDate = new Date(getEndDate(props.meterDuration, ""));
  const [didConfettiPop, setDidConfettiPop] = useState(false);

  /**
   * @returns remaining time of goal
   */
  const getFormatedDate = function () {
    const today = new Date();
    return formatDistance(today, endDate);
  };
  const [chartData, setChartData] = useState(ChartData.data);

  /**
   * this useEffect will run on first render
   */
  useEffect(() => {
    const newRemaining = getFormatedDate();
    setRemaining(newRemaining.replace("about ", ""));
    getGraph();
  }, []);
  useEffect(() => {
    getGraph();
  }, [props.meterValue]);

  const avatarStyles = AvatarStyles({ linePosition })();
  const goalIconStyles = getGoalIconStyles(linePosition);
  const [reward, setReward] = useState<any>();
  const meterGoal = props.meterGoal;
  const [trendingDir, setTrendingDir] = useState(false);

  /**
   * this function is used to pop confetti if goal was meet
   */
  const confettiRequest = () => {
    if (meterPosition >= meterGoal) {
      reward && reward.rewardMe();
      setDidConfettiPop(true);
    }
  };

  const config = {
    angle: 90,
    decay: 0.91,
    spread: 180,
    startVelocity: 35,
    elementCount: 500,
    elementSize: 8,
    lifetime: 200,
    zIndex: 100,
    springAnimation: true,
  };
  /**
   * this useEffect is for rerendering based on
   * @chartData
   * @meterPosition
   * and this will set @trendingDir and @status
   */
  useEffect(() => {
    if (chartData) {
      const day = 6;
      const yesterday = 5;
      const trending = chartData[day].uv > chartData[yesterday].uv;
      setTrendingDir(trending);
    }
    if (props.flip) {
      const percentage = (meterGoal / meterPosition) * 80;
      const goalStatus = percentage >= 80 ? 2 : percentage > 20 ? 1 : 0;
      setStatus(goalStatus);
    } else {
      const goalStatus =
        meterPosition >= props.meterGoal
          ? 2
          : meterPosition >= props.meterGoal / 4
          ? 1
          : 0;
      setStatus(goalStatus);
    }
  }, [chartData, meterPosition]);

  /**
   * this is get graph function use to set trending lines value
   * this fuction is used set @chartData and @meterPosition
   */
  const getGraph = async () => {
    if (!didConfettiPop) {
      props.confetti && confettiRequest();
    }
    const arrDays: any = [];

    const newObj = Object.entries(props.meterValue);
    const newDate: any = newObj.find(
      (obj: any) => obj[0] === props.meterKpiId
    )?.[1];
    let score: number = 0;
    const dateEnd = getEndDate(props.meterDuration, "yyyy-MM-dd");
    const dateStart = getStartDate(props.meterDuration, "yyyy-MM-dd");
    let weekEndDate = getStartDate("Daily", "yyyy-MM-dd");
    let weekStartDate = getWeekStartDate("yyyy-MM-dd");

    for (let i = 0; i < 7; i++) {
      let now = new Date();
      const result = startOfDay(now.setDate(now.getDate() - i));

      const newDay = {
        name: moment(startOfDay(now)).format("ddd"),
        date: moment(startOfDay(now)).format("YYYY-MM-DD"),
        uv: 0,
      };
      arrDays.push(newDay);
    }
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
          if (dateCheck(weekStartDate, weekEndDate, formattedDate)) {
            var foundIndex = arrDays.findIndex(
              (x: any) =>
                moment.utc(x.date).format() == moment.utc(date).format()
            );
            if (foundIndex >= 0) {
              arrDays[foundIndex].uv = parseFloat(newDate[item].toFixed(2));
            }
          }
          if (dateCheck(dateStart, dateEnd, formattedDate)) {
            score += parseFloat(newDate[item].toFixed(2));
          }
        }); //sorting dates and its coresponding values
      setMeterPosition(score);
      setChartData(arrDays.reverse());
    } else {
      setChartData(arrDays.reverse());
    }
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

  return (
    <TrackVisibility partialVisibility once>
      {({ isVisible }) => (
        <div className={styles.root}>
          <Paper className={styles.paper}>
            <Container>
              <Grid container direction="row" className={styles.container}>
                <Grid
                  container
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Grid item xs={"auto"} className={styles.headerContainer}>
                    {/*<props.meterIcon />*/}
                    <Tooltip
                      classes={{
                        tooltip: styles.tooltip,
                        popper: styles.popper,
                      }}
                      title={`${props.meterTitle}`}
                      placement="bottom"
                    >
                      <Typography className={styles.labelTypography}>
                        {props.meterTitle}
                      </Typography>
                    </Tooltip>
                  </Grid>
                  <Grid item xs={"auto"} className={styles.statusContainer}>
                    <CircleIcon style={{ fill: statusColor }} />
                    <Typography>{statusTitle}</Typography>
                  </Grid>
                </Grid>
                <Grid
                  container
                  direction="row"
                  justifyContent="flex-start"
                  alignItems="center"
                  className={styles.durationContainer}
                  spacing={4}
                >
                  <Grid
                    item
                    xs={props.confetti ? "auto" : 2}
                    className={styles.durationFlexBasic}
                  >
                    <Typography className={styles.durationHeaderText}>
                      DURATION
                    </Typography>
                    <Typography className={styles.durationBodyText}>
                      {props.meterDuration}
                    </Typography>
                  </Grid>
                  <Grid item xs={"auto"} className={styles.durationFlexBasic}>
                    <Divider
                      orientation="vertical"
                      className={styles.durationDivider}
                    />
                  </Grid>

                  <Grid item xs={"auto"} className={styles.durationFlexBasic}>
                    <Typography className={styles.durationHeaderText}>
                      TIME REMAINING
                    </Typography>
                    <Typography className={styles.durationBodyText}>
                      {remaining}
                    </Typography>
                  </Grid>
                  <Grid item xs={"auto"} className={styles.durationFlexBasic}>
                    <Divider
                      orientation="vertical"
                      className={styles.durationDivider}
                    />
                  </Grid>
                  <Grid item xs={"auto"} className={styles.durationFlexBasic}>
                    <Typography className={styles.durationHeaderText}>
                      VALUE
                    </Typography>
                    <Typography className={styles.durationBodyText}>
                      {props.meterCoins} Points
                    </Typography>
                  </Grid>
                </Grid>
                <Grid item xs={12} className={styles.meterContainer}>
                  <>
                    <div className={`${styles.redMeter} ${styles.left}`}></div>
                    <div className={styles.yellowMeter}></div>
                    <div
                      className={`${styles.greenMeter} ${styles.right}`}
                    ></div>
                  </>

                  <span
                    className={styles.meterLine}
                    style={{
                      left: linePosition,
                      backgroundColor: darken(statusColor, 0.1),
                    }}
                  ></span>
                  <span
                    className={`${styles.goalLine} ${styles.goalLineRight}`}
                  ></span>
                  <StyledAvatar
                    className={avatarStyles.root}
                    src={props.imageUrl}
                  />
                  <Typography
                    className={styles.positionText}
                    style={{
                      left: `calc(${linePosition} + 40px)`,
                    }}
                  >
                    {parseFloat(meterPosition.toFixed(2))} of Goal
                  </Typography>

                  <Typography
                    className={`${styles.goalText} ${styles.goalTextRight}`}
                  >
                    GOAL
                    <br />
                    {props.meterGoal}
                  </Typography>
                </Grid>
                <Grid
                  container
                  direction="row"
                  justifyContent="center"
                  alignItems="center"
                >
                  <Reward
                    ref={(ref) => {
                      setReward(ref);
                    }}
                    type="confetti"
                    config={config}
                  >
                    <Typography className={styles.confetti}>
                      confetti
                    </Typography>
                  </Reward>
                </Grid>
                <div className={styles.graphCont}>
                  <Grid
                    container
                    direction="row"
                    alignItems="center"
                    spacing={2}
                  >
                    <Grid item xs={2} className={styles.line}>
                      <PerformanceLineTrend direction={trendingDir} />
                    </Grid>
                    <Grid item xs={10} className={styles.chart}>
                      <PerformanceLineChart
                        data={chartData}
                        direction={trendingDir}
                      />
                    </Grid>
                  </Grid>
                </div>
              </Grid>
            </Container>
          </Paper>
        </div>
      )}
    </TrackVisibility>
  );
};

GoalsMeter.defaultProps = {
  imageUrl: "https://picsum.photos/1000",
  meterTitle: "Customer Satisfaction Score",
  meterIcon: CustSatIcon,
  meterStatus: MeterStatus.MET,
  meterGoal: 100,
  meterPosition: 105,
};

export default GoalsMeter;
