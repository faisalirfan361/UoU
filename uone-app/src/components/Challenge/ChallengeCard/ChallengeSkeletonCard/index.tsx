import React  from "react";
import { Box } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";

import useChallengeCardStyles from "../style";

const ChallengeSkeletonCard  = () => {
  const classes = useChallengeCardStyles();

  return (
    <Box className={classes.root}>
      <Skeleton variant="text" width={"100%"} height={75} />
      <Skeleton variant="rect" width={"100%"} height={397} />
      <Skeleton variant="text" width={"100%"} height={67} />
    </Box>
  );
};

export default ChallengeSkeletonCard;
