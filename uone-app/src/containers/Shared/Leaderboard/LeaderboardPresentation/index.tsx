import React, { memo, useRef } from "react";
import Grid from "@material-ui/core/Grid";
import { Typography } from "@material-ui/core";
import clsx from "clsx";
import { useFullscreen, useToggle } from "react-use";

import logo from "assets/img/logo2.svg";
import useLeaderboardPresentationStyles from "./style";
import { LeaderboardPresentationProps } from "./types";
import LeaderboardMainContainer from "../LeaderboardMainContainer";
import ButtonAction from "components/ButtonAction";

const LeaderboardPresentation: React.FC<LeaderboardPresentationProps> = () => {
  const classes = useLeaderboardPresentationStyles();

  const scrollableId = "leaderboard-presentation-div";
  const leaderboardElement = useRef(null);
  const [show, toggleFullScreen] = useToggle(false);
  const isFullscreen = useFullscreen(leaderboardElement, show, {
    onClose: () => toggleFullScreen(false),
  });

  return (
    <div className={classes.root}>
      <Grid container direction="row" justifyContent="flex-end">
        <Grid item>
          <ButtonAction
            handleOnClick={() => {
              toggleFullScreen();
            }}
          >
            Fullscreen
          </ButtonAction>
        </Grid>
      </Grid>

      <div
        ref={leaderboardElement}
        className={clsx({ [classes.fullScreenContainer]: isFullscreen })}
        id={scrollableId}
      >
        <Grid container direction="row" justifyContent="center">
          <Grid item className={classes.logoContainer}>
            {isFullscreen ? (
              <img className={classes.logo} alt="Heyday Now logo" src={logo} />
            ) : (
              ""
            )}
          </Grid>
        </Grid>
        <Grid container direction="row" justifyContent="center">
          <Grid item xs={12} sm={12} md={12} lg={6}>
            {isFullscreen ? (
              <Typography className={classes.leaderboardTitle}>
                Leaderboard
              </Typography>
            ) : (
              ""
            )}
          </Grid>
        </Grid>
        <div>
          <LeaderboardMainContainer
            scrollableElementId={isFullscreen ? scrollableId : "content"}
          />
        </div>
      </div>
    </div>
  );
};

export default memo(LeaderboardPresentation);
