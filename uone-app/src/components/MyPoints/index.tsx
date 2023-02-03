import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";
import Typography from "@material-ui/core/Typography";

import IProps from "./types";

import useStyles from "./style";

const MyPoints: FC<IProps> = ({ balance, cumulative }) => {
  const classes = useStyles();

  return (
    <Box className={classes.container}>
      <Box className={classes.containerTop}>
        <Box className={classes.semiCircleTop}>
          <Typography className={classes.subtitle}>Balance</Typography>
          <Typography className={classes.title}>{balance}</Typography>
        </Box>
      </Box>
      <Box className={classes.containerBottom}>
        <Box className={classes.semiCircleBottom}>
          <Typography className={classes.title}>{cumulative}</Typography>
          <Typography className={classes.subtitle}>Cumulative</Typography>
        </Box>
      </Box>
    </Box>
  );
};

export default memo(MyPoints);
