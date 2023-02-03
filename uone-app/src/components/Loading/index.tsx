import React, { FC, memo } from "react";
import CircularProgress from "@material-ui/core/CircularProgress";
import Box from "@material-ui/core/Box";

import IProps from "./types";
import useStyles from "./style";

const Loading: FC<IProps> = ({ isInProgress }) => {
  const classes = useStyles();

  if (!isInProgress){
    return null;
  }

  return (
    <Box className={classes.root}>
      <Box className={classes.progressContainer}>
        <CircularProgress />
      </Box>
    </Box>
  );
};

export default memo(Loading);
