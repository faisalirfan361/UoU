import React, { memo, useEffect, useRef, useState } from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { API } from "aws-amplify";

import TeamPerformanceCard from "../../../components/PerformanceCards/TeamPerformanceCard";
import config from "../../../config";
import { Style, autoCompleteStyle } from "./style";
import TeamsPerformanceDialog from "./TeamPerformanceDialog";
import { useDepartments } from "../../../hooks/useDepartments";
import DepartmentSelector from "../../../components/DepartmentSelector";
import CircularProgress from "@material-ui/core/CircularProgress";
import InfiniteScroll from "react-infinite-scroll-component";
import { Container } from "@material-ui/core";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
interface TeamData {
  departmentId: string;
  department_name: string;
  client_id: number;
  manager_first_name: string;
  manager_last_name: string;
  [key: string]: any;
}

interface GoalAttributes {
  goalName: string;
  metricType: string;
  goalValue: number;
  minNumber: number;
  maxNumber: number;
  minInfinite: boolean;
  clientId: string;
  departmentId: string;
  weight: number;
  maxInfinite: boolean;
  flip: boolean;
  metricDuration: string;
  points: number;
  indicator: string;
  status?: boolean;
}

interface Goal {
  created_at: string;
  departmentId: string;
  entityId: string;
  clientId: string;
  type: string;
  attributes: GoalAttributes;
}

const TeamsPerformance = (props: any) => {
  const classes = Style();

  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();
  const { departmentId } = useRecoilValue(userAtom);
  const [depsInfoSummarize, setDepsInfoSummarize] = useState<TeamData[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [departmentIdForModal, setDepartmentIdForModal] = useState("0");
  const [loading, setLoading] = React.useState(false);
  const [page, setPage] = React.useState(0);
  const [defaultGoal, setDefaultGoal] = React.useState([]);

  const myRef = useRef(null);

  const openDialog = (departmentId: string) => {
    setDepartmentIdForModal(departmentId);
    setIsDialogOpen(true);
  };
  const closeDialog = () => {
    setDepartmentIdForModal("0");
    setIsDialogOpen(false);
  };

  useEffect(() => {
    getGoals();
    setLoading(true);
    setDepsInfoSummarize([]);
    Promise.all([getTeamsData()])
      .then((values: any) => {
        if (!(values[0] > 0)) {
          Promise.all([getTeamsData()])
            .then(() => {
              setLoading(false);
            })
            .catch(() => {
              setLoading(false);
            });
        } else setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, [selectedDepartment]);

  /**
   * this is an api call function
   * to get team goals
   */
  const getGoals = async () => {
    let path = `/entity/get-goals?department=${departmentId}`;
    const data = await API.get(config.apiGateway.NAME, path, {});
    const activeGoals = data.filter((goal: Goal) =>
      typeof goal.attributes.status === "undefined"
        ? true
        : goal.attributes.status
    );
    const goals = activeGoals.map((goal: Goal) => {
      return {
        code: goal.attributes.goalName,
        title: goal.attributes.goalName,
        goal: goal.attributes.points,
        id: goal.attributes.indicator,
        obtained: 0,
      };
    });
    setDefaultGoal(goals);
  };
  /**
   * this is an api call function
   * to get teams summmary
   */
  const getTeamsData = async (pageNum?: number) => {
    let path = `/entity/get-teams-summary`;

    if (selectedDepartment.department_id !== "-1") {
      path = path + `?groupId=${selectedDepartment.department_id}`;
    }
    if (pageNum) {
      const sign = selectedDepartment.department_id !== "-1" ? "&" : "?";
      path = path + `${sign}lastKey=${pageNum}`;
    }
    const result = await API.get(config.apiGateway.NAME, path, {});

    const skipDepartments: string | any[] = [];

    if (result && result.data && result.data.length) {
      const deps = result.data.filter((team: any) => {
        return !skipDepartments.includes(team.department_id);
      });
      setDepsInfoSummarize((prev) => prev.concat(deps));
    }
    return result.data.length;
  };
  const hasMore = (depsInfoSummarize.length &&
    depsInfoSummarize.length % 10 === 0) as boolean; //
  const loadMore = async () => {
    setLoading(true);
    setPage(page + 1);
    await Promise.all([getTeamsData(page + 1)])
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  };
  const Loading = (loading: boolean) =>
    loading ? (
      <div className={classes.loading}>
        <CircularProgress className={classes.CircularProgress} />
      </div>
    ) : (
      <></>
    );

  const onDepartmentSelect = (dep: any) => {
    setPage(0);
    setSelectedDepartment(dep);
  };
  return (
    <>
      <div className={classes.root}>
        <Grid container spacing={0}>
          <Grid item xs={12} sm={12} className={classes.dropdownSection}>
            <DepartmentSelector
              options={departments}
              defaultOption={defaultDepartment}
              onSelect={(dep) => {
                onDepartmentSelect(dep);
              }}
            />
          </Grid>
        </Grid>
        <InfiniteScroll
          next={loadMore}
          hasMore={hasMore}
          dataLength={depsInfoSummarize.length}
          loader={Loading(loading)}
          scrollableTarget={myRef}
          className={classes.infiniteScroll}
        >
          <Grid
            container
            spacing={4}
            ref={myRef}
            className={classes.containDeps}
            xs={12}
          >
            {depsInfoSummarize.map((team, indexTeam: number) => {
              return (
                <Grid
                  key={`team-performance-${indexTeam}`}
                  item
                  xs={12}
                  sm={6}
                  md={6}
                  lg={4}
                  xl={4}
                >
                  <TeamPerformanceCard
                    team={team}
                    defaultGoal={defaultGoal}
                    teamDetailsFunc={() => {
                      openDialog(team.entityId);
                    }}
                  />
                </Grid>
              );
            })}
            {depsInfoSummarize.length === 0 && !loading && (
              <div>There are no teams with this department.</div>
            )}
          </Grid>

          {loading && Loading}
        </InfiniteScroll>
      </div>
      <TeamsPerformanceDialog
        departmentId={departmentIdForModal}
        isDialogOpen={isDialogOpen}
        closeDialog={closeDialog}
      />
    </>
  );
};

export default memo(TeamsPerformance);
