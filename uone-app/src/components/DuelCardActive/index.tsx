import React, { useState } from "react";
import DuelProps from "./types";
import {
  Grid,
  Box,
  Paper,
  Typography,
  useTheme,
  useMediaQuery,
} from "@material-ui/core";
import _get from "lodash.get";
import { ButtonAcceptDuel } from "components";
import useDuelActiveStyle from "./style";
import ActiveDuelPostImg from "components/Posts/PostComponents/PostImageSection/ActiveDuelPostImg";
import DuelCardActiveNameDetail from "components/DuelCardActiveNameDetail";
import ActiveDuelPostImgMobile from "components/Posts/PostComponents/PostImageSection/ActiveDuelPostImgMobile";
import _, { sortBy } from "lodash";
import { formatDistance } from "date-fns";
import { Indicator, useIndicator } from "hooks/useIndicators";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import config from "config";

const randInt = (max: number) => {
  return Math.floor(Math.random() * max);
};

const DuelCard: React.FC<DuelProps> = ({ duel }) => {
  const { userId, bannerImages, avatarImages, clientId } =
    useRecoilValue(userAtom);
  const { indicatorsData } = useIndicator(clientId);
  const flip = indicatorsData.find(
    (kpi: Indicator) => duel?.kpi_id === kpi.entityId
  )?.attributes.flip;
  let users = flip
    ? sortBy(duel.profiles, "score")
    : sortBy(duel.profiles, "score").reverse();

  const getAvatarImage = (imageUserId: string) => {
    let avatarUrl = `${config.targetBucketUrl}images/${imageUserId}/avatars/current.png`;
    if (userId == imageUserId) {
      avatarUrl = `${config.targetBucketUrl}${avatarImages?.keys.large}`;
    }
    return avatarUrl;
  };

  const getBannerImage = (imageUserId: string) => {
    let avatarUrl = `${config.targetBucketUrl}images/${imageUserId}/banners/current.png`;
    if (userId == imageUserId) {
      avatarUrl = `${config.targetBucketUrl}${bannerImages?.keys.large}`;
    }
    return avatarUrl;
  };

  const defaultProfile = {
    firstName: "",
    lastName: "",
    entityId: "",
    score: 0,
  };
  
  const agent1 = duel.profiles.length > 0 ? duel.profiles[0] : defaultProfile;
  const agent2 = duel.profiles.length > 1 ? duel.profiles[1] : defaultProfile;
  
  const detailProps = {
    agent1: {
      name: `${agent1.firstName} ${agent1.lastName}`,
      avatarImage: getAvatarImage(agent1.entityId),
      bannerImage: getBannerImage(agent1.entityId),
      entityId: agent1.entityId,
      score: agent1.score
    },
    agent2: {
      name: `${agent2.firstName} ${agent2.lastName}`,
      avatarImage: getAvatarImage(agent2.entityId),
      bannerImage: getBannerImage(agent2.entityId),
      entityId: agent2.entityId,
      score: agent2.score
    },
  };

  const classes = useDuelActiveStyle();
  const theme = useTheme();
  const xs = useMediaQuery(theme.breakpoints.up("sm"));
  const today = new Date();
  const duration = formatDistance(today, new Date(duel.end_date));
  return (
    <>
      <Grid container direction="row" className={classes.duelCardWrapper}>
        <Paper className={classes.duelContainer} elevation={2}>
          <DuelCardActiveNameDetail
            name={duel.title}
            coins={duel.winnerPoints}
            statusText={"ACTIVE"}
            kpi={duel.kpi_name || "new"}
            duration={duration}
          />
          <Grid item xs={12} className={classes.duelContent}>
            {xs ? (
              <ActiveDuelPostImg
                userOneProfileImgSrc={detailProps.agent1.avatarImage}
                userOneCoverImgSrc={detailProps.agent1.bannerImage}
                userTwoProfileImgSrc={detailProps.agent2.avatarImage}
                userTwoCoverImgSrc={detailProps.agent2.bannerImage}
                user={detailProps.agent2}
                opponent={detailProps.agent1}
              />
            ) : (
              <Grid item xs={12} className={classes.duelContentMobile}>
                <ActiveDuelPostImgMobile
                  userOneProfileImgSrc={detailProps.agent1.avatarImage}
                  userOneCoverImgSrc={detailProps.agent1.bannerImage}
                  userTwoProfileImgSrc={detailProps.agent2.avatarImage}
                  userTwoCoverImgSrc={detailProps.agent2.bannerImage}
                  user={detailProps.agent2}
                  opponent={detailProps.agent1}
                />
              </Grid>
            )}

            <Box className={classes.msgContainer}>
              <Grid item xs={12}>
                <Typography className={classes.msgHeading}>Message</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography className={classes.msgContent}>
                  {duel.description}
                </Typography>
              </Grid>
            </Box>
          </Grid>
        </Paper>
      </Grid>
    </>
  );
};

export default DuelCard;
