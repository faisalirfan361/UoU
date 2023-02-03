import React, { memo, useEffect, useMemo, useRef, useState } from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { API } from "aws-amplify";

import { TeamPerformanceProps } from "./types";
import useStyles from "./style";
import UserPerformanceDialog from "./UserPerformanceDialog";
import UserPerformanceCard from "../../../components/PerformanceCards/UserPerformanceCard";
import config from "../../../config";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import { Goal } from "components/GoalCard/types";
import InfiniteScroll from "react-infinite-scroll-component";
import CircularProgress from "@material-ui/core/CircularProgress";
import { isEmpty } from "lodash";

const PAGE_SIZE = 10;

interface User {
  userId: string;
  username: string;
  firstName: string;
  lastName: string;
  profileImg: string;
  [key: string]: any;
  performance: Performance;
}
interface Performance {
  kpiAverages: any;
  kpisState: any;
}
const TeamPerformance: React.FC<TeamPerformanceProps> = ({
  hideTitle,
  departmentId,
}) => {
  const classes = useStyles();
  const [users, setUsersList] = useState<User[]>([]);
  const [unFilterUsers, setUnFilterUsers] = useState<User[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [userForModal, setUserForModal] = useState(null);
  const [kpiSettings, setKpiSettings] = useState<any[]>([]);
  const [page, setPage] = React.useState(0);

  const [loading, setLoading] = useState(true);
  const myRef = useRef(null);
  const openDialog = (user: any) => {
    setUserForModal(user);
    setIsDialogOpen(true);
  };
  const closeDialog = () => {
    setUserForModal(null);
    setIsDialogOpen(false);
  };

  const getUsersKpis = async (lastKey?: number) => {
    let path = `/entity/get-team-summary`;
    const data = await API.post(config.apiGateway.NAME, path, {
      body: { departmentId: departmentId, lastKey: lastKey, limit: PAGE_SIZE },
    });
    if (data && Array.isArray(data)) {
      setUnFilterUsers((prev) => prev.concat(data));
      setUsersList((prev) =>
        prev.concat(data.filter((user: any) => user.performance))
      );
    }
  };

  const getGoals = async () => {
    let path = `/entity/get-goals?department=${departmentId}`;
    const data = await API.get(config.apiGateway.NAME, path, {});
    const activeGoals = data.filter((goal: Goal) =>
      typeof goal.attributes.status === "undefined"
        ? true
        : goal.attributes.status
    );
    setKpiSettings(activeGoals);
  };

  useEffect(() => {
    loadGoalsAndUsers();
  }, []);

  const loadGoalsAndUsers = async () => {
    setLoading(true);
    await getGoals();
    await getUsersKpis()
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  };

  const hasMore = (users.length && users.length % PAGE_SIZE === 0) as boolean;

  const Loading = (loading: boolean) =>
    loading ? (
      <div className={classes.loading}>
        <CircularProgress className={classes.CircularProgress} />
      </div>
    ) : (
      <></>
    );
  const loadMore = async () => {
    setLoading(true);
    setPage(page + 1);
    getUsersKpis(page + 1)
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  };

  return (
    <>
      <div className={classes.root}>
        {!hideTitle && (
          <Grid
            container
            direction="row"
            style={{ display: hideTitle ? "none" : "block" }}
          >
            <Grid item xs={12} className={classes.titleContainer}>
              <Typography
                className={classes.subtitle1}
                gutterBottom
                variant="subtitle1"
              >
                Team Members
              </Typography>
            </Grid>
          </Grid>
        )}

        <InfiniteScroll
          scrollableTarget={"team-permomance-modal-container"}
          className={classes.infiniteScroll}
          key={users.length}
          dataLength={users.length}
          next={loadMore}
          hasMore={hasMore}
          loader={Loading(false)}
        >
          <Grid container className={classes.root} direction="row" spacing={4}>
            {!isEmpty(users) ? (
              users.map((user: any, index: number) => {
                return (
                  <Grid item xs={12} sm={6} md={6} lg={4} xl={4} key={index}>
                    <UserPerformanceCard
                      user={user}
                      kpiSettings={kpiSettings}
                      userDetailsFunc={() => {
                        openDialog(user);
                      }}
                    />
                  </Grid>
                );
              })
            ) : (
              <>
                {!loading && (
                  <div className={classes.text}>
                    No assigned members for this team.
                  </div>
                )}
              </>
            )}
          </Grid>
          {Loading(loading)}
        </InfiniteScroll>
      </div>

      <UserPerformanceDialog
        user={userForModal}
        closeDialog={closeDialog}
        isDialogOpen={isDialogOpen}
      />
    </>
  );
};

export default memo(TeamPerformance);
