import React, { FC } from "react";
import { Box, Grid, Typography } from "@material-ui/core";

import ChallengeDescriptionProps from "./types";
import useStyles from "./style";
import {getLongFormattedDateTime} from "../../../../../utils/getStandardFormattedDateTime"


const ChallengeDescription: FC<ChallengeDescriptionProps> = ({ kpiName,  endDate }) => {
  const classes = useStyles();
  const formatedDate = getLongFormattedDateTime(endDate);

  return (
    <Box className={classes.root}>
      <Grid container spacing={0}>
        <Grid item xs={6} >
          <Typography align={"center"} className={`label`}>KPI</Typography>
          <Typography align={"center"} className={`info`}>{kpiName}</Typography>
        </Grid>
        <Grid item xs={6}>
          <Typography align={"center"} className={`label`}>END DATE & TIME</Typography>
          <Typography align={"center"} className={`info`}>{formatedDate}</Typography>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ChallengeDescription;
