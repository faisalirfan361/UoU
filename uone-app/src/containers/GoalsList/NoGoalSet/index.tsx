import { Grid, Typography } from "@material-ui/core";
import React, { useState } from "react";
import NoGoalSetProps from "./types";
import useNoGoalSetStyle from "./styles";

const NoGoalSet: React.FC<NoGoalSetProps> = ({ onCreateGoalClick }) => {
  const classes = useNoGoalSetStyle();
  return (
    <>
      <Grid container direction="row" justifyContent="center">
        <Grid item xs={12}>
          <Typography className={classes.headingText}>
            You have no Goals set.
          </Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography className={classes.text}>
            Change the department above to create specific goals for that group.
          </Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography
            className={classes.goalTextBtn}
            onClick={onCreateGoalClick}
          >
            Create Goal
          </Typography>
        </Grid>
      </Grid>
    </>
  );
};

export default NoGoalSet;
