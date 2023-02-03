import React, { memo } from "react";
import Grid from "@material-ui/core/Grid";

import useStyles from "./style";
import { LeaderboardWithFilterProps } from "./types";
import { useDepartments } from "../../../../hooks/useDepartments";
import DepartmentSelector from "../../../../components/DepartmentSelector";
import LeaderboardMainContainer from "../LeaderboardMainContainer";

const Leaderboard: React.FC<LeaderboardWithFilterProps> = () => {
  const classes = useStyles();

  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();

  return (
    <>
      <div className={classes.root}>
        <Grid container direction="row">
          <Grid item xs={12}>
            <div className={classes.departmentsSelector}>
              <DepartmentSelector
                options={departments}
                defaultOption={defaultDepartment}
                onSelect={setSelectedDepartment}
              />
            </div>
          </Grid>
        </Grid>
        <Grid container direction="row" justifyContent="center">
          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.leaderBoard}>
            <LeaderboardMainContainer department={selectedDepartment} />
          </Grid>
        </Grid>
      </div>
    </>
  );
};

export default memo(Leaderboard);
