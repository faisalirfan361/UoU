import React, { FC } from "react";

import IProps from "./types";

import { Box, Typography, Grid } from "@material-ui/core";

import useStyles from "./style";
import FlagIcon from "@material-ui/icons/Flag";
import AssessmentIcon from "@material-ui/icons/Assessment";
import formatDistance from "date-fns/formatDistance";

const getIcon = function (type: string, iconClass: any) {
  switch (type) {
    case "FLAG":
      return <FlagIcon className={iconClass} />;
    case "MOUNT":
      return <AssessmentIcon className={iconClass} />;
  }
};

const getFormatedDate = function (postDate: Date) {
  const today = new Date();
  return formatDistance(postDate, today, {
    addSuffix: true,
  });
};

const PostTitle: FC<IProps> = ({ postTitle, postDate, iconType }) => {
  const classes = useStyles();

  const formatedDate: string = getFormatedDate(postDate);

  return (
    <Box className={classes.root}>
      <Grid container spacing={0}>
        <Grid item xs={6} className={classes.postTitle}>
          <Typography className={classes.postTitle}>
            {getIcon(iconType, classes.thumbnailImg)}
            <span>{postTitle}</span>
          </Typography>
        </Grid>
        <Grid item xs={6} className={classes.postDate}>
          <Typography className={classes.postDate}>{formatedDate}</Typography>
        </Grid>
      </Grid>
    </Box>
  );
};

export default PostTitle;
