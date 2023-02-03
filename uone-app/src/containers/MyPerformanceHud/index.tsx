import API from "@aws-amplify/api";
import {
  Box,
  Card,
  CardContent,
  Divider,
  Grid,
  Typography,
} from "@material-ui/core";

import ChartData from "components/GoalsMeter/chartData";
import PerformanceBadges from "components/PerformanceBadges";
import { PerformanceLineChart } from "components/PerformanceLineChart";
import { PerformanceLineTrend } from "components/PerformanceLineTrend";
import { useEffect, useState } from "react";
import { useRecoilValue } from "recoil";
import config from "../../config";
import { userAtom } from "state";
import { Badges } from "services/Api";
import startOfWeek from "date-fns/startOfWeek";
import startOfMonth from "date-fns/startOfMonth";
import { endOfDay, startOfDay } from "date-fns";
import startOfYear from "date-fns/startOfYear";
import format from "date-fns/format";
import { split, sortBy } from "lodash";
import moment from "moment";

export default function MyPerformanceHud() {
  const [chartData, setChartData] = useState(ChartData.data);
  const [myBadges, setMyBadges] = useState([]);
  const [trendingDir, setTrendingDir] = useState(false);
  const { userId } = useRecoilValue(userAtom);

  /**
   * this is an api call function
   *  to get user badges
   */
  const getMyBadges = async () => {
    const { data } = await Badges.single(userId);
    setMyBadges(data);
  };

  useEffect(() => {
    getGraph();
    getMyBadges();
  }, []);

  useEffect(() => {
    if (chartData) {
      const day = 6;
      const yesterday = 5;
      const trending = chartData[day].uv > chartData[yesterday].uv;
      setTrendingDir(trending);
    }
  }, [chartData]);

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
    if (cDate <= lDate && cDate >= fDate) return true;
    return false;
  };

  /**
   *
   * @param dateFormate is the formate of the date
   * @returns this return the date from last week
   */
  const formateKPIDate = (date: string, dateFormate: string): string => {
    return format(endOfDay(new Date(date)), dateFormate);
  };

  /**
   *  this is a api call function
   *  gets user performance based on kpis
   *  by getting Kpi Average from response
   *  then calculates and get KPI Average Graph data
   */
  const getGraph = async () => {
    let path = `/game/performance`;
    const data = await API.post(config.apiGateway.NAME, path, {
      body: { userId: userId },
    });
    const arrDays: any = [];
    const weekStartDate = getWeekStartDate("yyyy-MM-dd");
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
    if (data.statusCode == 200) {
      const weekEndDate = getStartDate("Daily", "yyyy-MM-dd");
      const kpiAverages = JSON.parse(data.body).data.kpiAverages;
      if (kpiAverages) {
        Object.keys(kpiAverages)
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
                arrDays[foundIndex].uv = parseFloat(
                  kpiAverages[item].toFixed(2)
                );
              }
            }
          }); //sorting dates and its coresponding values
      }
    }
    setChartData(arrDays.reverse());
  };

  return (
    <Card>
      <CardContent>
        <Grid container alignItems="center" spacing={5}>
          <Grid item sm>
            <Typography variant="h6" gutterBottom>
              Average KPI Performance
            </Typography>
            <Grid container direction="row" alignItems="center" spacing={2}>
              <Grid item xs={2}>
                <PerformanceLineTrend direction={trendingDir} />
              </Grid>
              <Grid item xs={10}>
                <PerformanceLineChart
                  height={140}
                  data={chartData}
                  direction={trendingDir}
                />
              </Grid>
            </Grid>
          </Grid>
          <Divider orientation="vertical" flexItem />
          <Grid item sm>
            <Typography variant="h6" gutterBottom>
              Badges
            </Typography>
            <Grid container>
              <Grid item xs={12}>
                <PerformanceBadges badges={myBadges} minHeight={140} />
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
}
