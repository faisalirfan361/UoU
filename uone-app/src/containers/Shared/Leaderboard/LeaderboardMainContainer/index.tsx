import React, { useState, useEffect, memo } from "react";
import Grid from "@material-ui/core/Grid";
import InfiniteScroll from "react-infinite-scroll-component";

import useLeaderboardMainStyles from "./style";
import { LeaderboardContainerProps } from "./types";
import LeaderboardTable from "../LeaderboardTable";
import API from "@aws-amplify/api";
import config from "../../../../config";
import CircularProgress from "@material-ui/core/CircularProgress";
const PAGE_SIZE = 100;
const LeaderboardMainContainer: React.FC<LeaderboardContainerProps> = ({
  department = null,
  scrollableElementId = "content",
}) => {
  const classes = useLeaderboardMainStyles();
  const [leaderboardUsers, setLeaderboardUsers] = useState([] as any);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUsers();
  }, []);

  useEffect(() => {
    getUsers();
  }, [department]);

  /**
   *
   * @returns this will return loading component
   */
  const startLoading = (loading: boolean) => {
    return (
      <div className={classes.loading}>
        {loading && <CircularProgress className={classes.CircularProgress} />}
      </div>
    );
  };
  const getEndMessage = (usersCount: number) => {
    let message = "All the users have been displayed";
    if (usersCount === 0) {
      message = "Not users for selected department";
    }

    return (
      <div>{!loading && <p style={{ textAlign: "center" }}>{message}</p>}</div>
    );
  };
  /**
   * this is an api call function to get user information
   */
  const getUsers = async (userId?: string) => {
    setLoading(true);
    let path = `/entity/get-users`;
    if (department && department.department_id !== "-1") {
      path = path + `?groupId=${department.department_id}`;
    }
    if (userId) {
      const sign = department && department.department_id !== "-1" ? "&" : "?";
      path = path + `${sign}lastKey=${userId}`;
    }
    const data: [] = await API.get(config.apiGateway.NAME, path, {});
    if (data.length > 0) {
      const allData = data.map((item: any) => {
        if (item.uone_data?.points && item.uone_data?.points != "NULL")
          return { ...item };
        else {
          return {
            ...item,
            uone_data: { ...item.uone_data, points: 0 },
          };
        }
      });

      const concatUsers = leaderboardUsers.concat(allData);
      if (userId) {
        setLeaderboardUsers(concatUsers);
      } else {
        setLeaderboardUsers(allData);
      }
    } else {
      if (!userId) {
        setLeaderboardUsers([]);
      }
    }
    setLoading(false);
  };
  const hasMore = (leaderboardUsers.length &&
    leaderboardUsers.length % PAGE_SIZE === 0) as boolean;
  const lastUser = leaderboardUsers[leaderboardUsers.length - 1];
  const lastKey = lastUser && lastUser.entityId;
  return (
    <>
      <div className={classes.root}>
        <Grid container direction="row" justifyContent="center">
          <Grid
            item
            xs={12}
            sm={12}
            md={6}
            lg={6}
            className={classes.container}
          >
            <InfiniteScroll
              key={`${scrollableElementId}-key`}
              dataLength={leaderboardUsers.length}
              next={() => getUsers(lastKey)}
              hasMore={hasMore}
              loader={startLoading(false)}
              scrollableTarget={scrollableElementId}
              endMessage={getEndMessage(leaderboardUsers.length)}
              className={classes.infiniteScroll}
            >
              <LeaderboardTable users={leaderboardUsers} />
              {startLoading(loading)}
            </InfiniteScroll>
          </Grid>
        </Grid>
      </div>
    </>
  );
};

export default memo(LeaderboardMainContainer);
