import React, { memo, useEffect, useState } from "react";
import Grid from "@material-ui/core/Grid";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableBody from "@material-ui/core/TableBody";
import { Box, Typography } from "@material-ui/core";
import TableContainer from "@material-ui/core/TableContainer";
import { useRecoilValue } from "recoil";
import { userAtom } from "../../../../state";
import clsx from "clsx";
import { useTheme } from "@material-ui/styles";
import { Theme } from "@material-ui/core/styles";
import _, { sortBy } from "lodash";
import useStyles from "./style";
import LeaderboardTableProps from "./types";
import { TrendingUpArrow } from "../../../../components/Icons/TrendingUpArrow";
import { TrendingDownArrow } from "../../../../components/Icons/TrendingDownArrow";
import UOneTableHeader from "../../../../components/TableComponents/UOneTableHeader";
import ZebraTableRow from "../../../../components/TableComponents/ZebraTableRow";
import StyledAvatar from "../../../../components/StyledAvatar";
import API from "@aws-amplify/api";
import config from "../../../../config";
import {
  dateCheck,
  formateKPIDate,
  getStartDate,
  getWeekStartDate,
} from "../../../../utils/dateUtils";

const isLoggedUserOnLeaderboard = (
  userId: string,
  leaderboardUserId: string
) => {
  return userId == leaderboardUserId;
};

const getProgress = (kpiAverages: any) => {
  const arrDays = [
    { name: "", uv: 0 },
    { name: "Mon", uv: 0 },
    { name: "Tues", uv: 0 },
    { name: "Wed", uv: 0 },
    { name: "Thurs", uv: 0 },
    { name: "Fri", uv: 0 },
    { name: "", uv: 0 },
  ];
  const weekEndDate = getStartDate("Daily", "yyyy-MM-dd");
  const weekStartDate = getWeekStartDate("yyyy-MM-dd");
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
          arrDays[new Date(formattedDate).getDay()].uv = kpiAverages[item];
        }
      }); //sorting dates and its coresponding values
    const day = new Date().getDay();
    const yesterday = day === 0 ? day + 6 : day - 1;
    const dayBeforeYesterday = yesterday === 0 ? yesterday + 6 : yesterday - 1;
    if (arrDays[day].uv !== 0) {
      if (arrDays[day].uv > arrDays[yesterday].uv) {
        return "up";
      } else if (arrDays[day].uv < arrDays[yesterday].uv) {
        return "down";
      } else return "down";
    } else {
      if (arrDays[yesterday].uv > arrDays[dayBeforeYesterday].uv) {
        return "up";
      } else if (arrDays[yesterday].uv < arrDays[dayBeforeYesterday].uv) {
        return "down";
      } else return "down";
    }
  }
  return "down";
};

const getPerformanceIcon = (user: any, index: number, theme: Theme) => {
  const progress = getProgress(user.performance?.kpiAverages);
  if (progress === "up") {
    return (
      <TrendingUpArrow fill={`url(#trending-arrow-up-${index})`}>
        <defs>
          <linearGradient id={`trending-arrow-up-${index}`}>
            <stop
              offset="14.29%%"
              style={{ stopColor: `${theme.common.uoneLightBlue[500]}` }}
            />
            <stop
              offset="100%"
              style={{ stopColor: `${theme.common.uoneLightGreen[500]}` }}
            />
          </linearGradient>
        </defs>
      </TrendingUpArrow>
    );
  } else if (progress === "down") {
    return (
      <TrendingDownArrow fill={`url(#trending-arrow-up-${index})`}>
        <defs>
          <linearGradient id={`trending-arrow-up-${index}`}>
            <stop
              offset="-3%"
              style={{ stopColor: `${theme.common.uoneMediumPurple[500]}` }}
            />
            <stop
              offset="100%"
              style={{ stopColor: `${theme.common.uoneFrolyRed[500]}` }}
            />
          </linearGradient>
        </defs>
      </TrendingDownArrow>
    );
  }
};

const getLoggedUserPerformance = (user: any, theme: Theme) => {
  const progress = getProgress(user.performance?.kpiAverages);
  if (progress === "up") {
    return (
      <TrendingUpArrow fill={`${theme.palette.common.white}`}></TrendingUpArrow>
    );
  } else if (progress === "down") {
    return (
      <TrendingDownArrow
        fill={`${theme.palette.common.white}`}
      ></TrendingDownArrow>
    );
  }
};

const LeaderboardTable: React.FC<LeaderboardTableProps> = ({ users }) => {
  const { userId } = useRecoilValue(userAtom);
  const userList = sortBy(users, "uone_data.points").reverse();

  const theme = useTheme<Theme>();

  const classes = useStyles();

  if (users.length <= 0) return null;
  return (
    <TableContainer className={classes.root}>
      <Table stickyHeader aria-label="sticky table">
        <TableHead>
          <TableRow>
            <UOneTableHeader></UOneTableHeader>
            <UOneTableHeader>
              <Typography>Agent Name</Typography>
            </UOneTableHeader>
            <UOneTableHeader>
              <Typography>Points</Typography>
            </UOneTableHeader>
            <UOneTableHeader>
              <Typography>Performance</Typography>
            </UOneTableHeader>
          </TableRow>
        </TableHead>
        <TableBody>
          {userList.map((user: any, index: number) => {
            const isLoggedUser = isLoggedUserOnLeaderboard(
              userId,
              user.entityId
            );

            return (
              <ZebraTableRow
                key={`pointleader-row-${index}`}
                className={clsx({ [classes.ownScore]: isLoggedUser })}
              >
                <TableCell className={classes.number}>{`${index + 1
                  }.`}</TableCell>
                <TableCell>
                  <Grid container alignItems="center" wrap="nowrap" spacing={0}>
                    <Grid item>
                      <StyledAvatar
                        src={
                          config.targetBucketUrl +
                          `${user.attributes.avatarImages?.keys.medium}`
                        }
                        className={classes.userAvatar}
                      />
                    </Grid>
                    <Grid item xs zeroMinWidth>
                      <Typography className={classes.agentName} noWrap>
                        {user.mask
                          ? `${user.mask?.firstName} ${user.mask?.lastName} `
                          : `${user.attributes.firstName} ${user.attributes.lastName}`}
                      </Typography>
                    </Grid>
                  </Grid>
                </TableCell>
                <TableCell className={classes.points}>
                  {user.uone_data.points}
                </TableCell>
                <TableCell>
                  <Box className={classes.performanceIcon}>
                    {isLoggedUser
                      ? getLoggedUserPerformance(user, theme)
                      : getPerformanceIcon(user, index, theme)}
                  </Box>
                </TableCell>
              </ZebraTableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default memo(LeaderboardTable);
