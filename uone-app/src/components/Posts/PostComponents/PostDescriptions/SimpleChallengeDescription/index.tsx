import React, { FC } from "react";
import SimpleChallengeDescriptionProps from "./types";
import { Avatar, Box, Grid, Typography } from "@material-ui/core";
import useStyles from "./style";
import TimerIcon from "@material-ui/icons/Timer";
import StyledAvatar from "components/StyledAvatar";

const getIcon = function (type: string, avatarStyle: any, icon: any) {
  switch (type) {
    case "TIMER":
      return (
        <StyledAvatar className={avatarStyle}>
          <TimerIcon className={icon} />
        </StyledAvatar>
      );
  }
};

const SimpleChallengeDescription: FC<SimpleChallengeDescriptionProps> = ({
  iconType,
  details,
}) => {
  const classes = useStyles();

  return (
    <Box className={classes.root}>
      <Grid container spacing={0}>
        <Grid item xs={2} className={classes.iconSection}>
          {getIcon(iconType, classes.avatarStyle, classes.icon)}
        </Grid>
        <Grid item xs={10} className={classes.descSection}>
          <Typography className={"participants"}>
            <span className={"name"}>New Challenge</span>
          </Typography>
          <Typography className={"details"}>{details}</Typography>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SimpleChallengeDescription;
