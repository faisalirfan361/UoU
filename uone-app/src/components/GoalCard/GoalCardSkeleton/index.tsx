import React from "react";
import { Box } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";

import useGoalCardStyles from "../styles";

const GoalCardSkeleton = () => {
  const classes = useGoalCardStyles();

  return (
    <Box className={classes.card}>
      <Skeleton variant="rect" width={"100%"} height={190} />
    </Box>
  );
};

export default GoalCardSkeleton;
