import React, { useState } from "react";
import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";
import Paper from "@material-ui/core/Paper";
import DuelProps from "./types";
import Typography from "@material-ui/core/Typography";
import _get from "lodash.get";
import { ButtonAcceptDuel } from "components";
import useStyle from "./style";
import SimpleDuelPostImg from "components/Posts/PostComponents/PostImageSection/SimpleDuelPostImg";
import DuelCardScoreDetail from "components/DuelCardScoreDetail";
import DuelCardNameDetail from "components/DuelCardNameDetail";
import DuelCardKPIDetail from "components/DuelCardKPIDetail";
import _, { sortBy } from "lodash";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
import { DuelType } from "components/GameCard/type";
import { useIndicator, Indicator } from "hooks/useIndicators";
import { formatDistance } from "date-fns";

const randInt = (max: number) => {
  return Math.floor(Math.random() * max);
};

const DuelCard: React.FC<DuelProps> = ({ duel, refresh, filter }) => {
  const { userId, bannerImages, avatarImages, clientId } =
    useRecoilValue(userAtom);
  const { indicatorsData } = useIndicator(clientId);
  const flip = indicatorsData.find(
    (kpi: Indicator) => duel?.kpi_id === kpi.entityId
  )?.attributes.flip;
  let users = flip
    ? sortBy(duel.profiles, "score")
    : sortBy(duel.profiles, "score").reverse();
  /**
   * this is used to set avatar and banner images
   */
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
  };
  const agent1 = duel.profiles.length > 0 ? duel.profiles[0] : defaultProfile;
  const agent2 = duel.profiles.length > 1 ? duel.profiles[1] : defaultProfile;

  const detailProps = {
    agent1: {
      name: `${agent1.firstName} ${agent1.lastName}`,
      avatarImage: getAvatarImage(agent1.entityId),
      bannerImage: getBannerImage(agent1.entityId),
    },
    agent2: {
      name: `${agent2.firstName} ${agent2.lastName}`,
      avatarImage: getAvatarImage(agent2.entityId),
      bannerImage: getBannerImage(agent2.entityId),
    },
  };

  const { enqueueSnackbar } = useSnackbar();
  const classes = useStyle();
  const [buttonStatus, setButtonStatus] = useState(false);
  const status = duel.isAccepted
    ? duel.profiles[0].score === duel.profiles[0].score
      ? 0
      : 1
    : 2;

  /**
   *
   * @returns is an array of challenge user objects
   */
  const usersForDuel = () => {
    const result: any = users.map((user: any) => {
      return {
        user_id: user.entityId,
        first_name: user.firstName,
        last_name: user.lastName,
        team_id: user.departmentId,
      };
    });
    return result;
  };

  /**
   * this is handle accept fuction
   * get call on accept button click
   */
  const handleAccept = async () => {
    setButtonStatus(true);
    const data = {
      duelId: duel.gameId,
    };
    try {
      await API.post(config.apiGateway.NAME, "/game/accept-duel", {
        body: data,
      });
      if (refresh) refresh();
      enqueueSnackbar("Duel Accepted", SUCCESS_TOAST_OPTIONS);
    } catch (e) {
      enqueueSnackbar("Failed to accept duel", ERROR_TOAST_OPTIONS);
      setButtonStatus(false);
    }
  };

  /**
   * this is handle reject fuction
   * get call on reject button click for agent side
   */
  const handleReject = async () => {
    setButtonStatus(true);
    const challengesUser: any[] = usersForDuel();
    const data = {
      gameId: duel.gameId,
      kpi_id: duel.kpi_id,
      kpi_name: duel.kpi_name,
      clientId: duel.clientId,
      title: duel.title,
      description: duel.description,
      winnerPoints: duel.winnerPoints,
      start_date: duel.start_date,
      end_date: duel.end_date,
      challengesUser: challengesUser,
      isAccepted: duel.isAccepted,
      isDuel: duel.isDuel,
      isDeclined: true,
      user_id: duel.user_id,
    };
    try {
      await API.post(config.apiGateway.NAME, "/game/update", {
        body: data,
      });
      if (refresh) refresh();
      enqueueSnackbar("Duel Declined", SUCCESS_TOAST_OPTIONS);
    } catch (e) {
      enqueueSnackbar("Failed to decline duel", ERROR_TOAST_OPTIONS);
    }
    setButtonStatus(false);
  };

  /**
   * this function is used to get info about duel
   * if this duel is new and ready to get accept/reject
   */
  const isNewDuelAccept = () => {
    const opponent = duel.profiles.find(
      (user: any) => user.entityId === userId
    );
    return (
      userId !== duel.user_id &&
      opponent &&
      !duel.isAccepted &&
      !duel.isDeclined
    );
  };

  /**
   * this fuction is to find the current state of the duel
   * @param duel duel props
   * @returns the status of the duel
   */
  const getDuelStatus = (duel: DuelType, flip: boolean | undefined) => {
    const startDate = Date.parse(duel.start_date);
    const endDate = Date.parse(duel.end_date);
    const now = Date.parse(new Date().toISOString());

    const isComplete = duel.isComplete;
    const winnerProfile = duel.winnerProfile;

    if (startDate > now && !duel.isAccepted) {
      return 0; // return 1 if duel is New
    }
    if (isComplete && winnerProfile) {
      return 3; // return 3 if challenge have a winner
    } else if (isComplete) {
      return 2; // return 2 if challenge is Draw
    } else {
      return endDate > now ? 1 : 2; // return 1 if challenge is Active
    }
  };

  const checkActive = () => {
    return (
      filter !== "active" ||
      (filter === "active" && getDuelStatus(duel, flip) <= 1)
    );
  };

  const today = new Date();
  const duration = formatDistance(today, new Date(duel.end_date));

  return (
    <>
      <Grid container direction="row" className={classes.duelCardWrapper}>
        <Paper className={classes.duelContainer} elevation={2}>
          <DuelCardNameDetail
            name={duel.title}
            coins={duel.winnerPoints}
            status={getDuelStatus(duel, flip)}
          />
          <Grid item xs={12} className={classes.duelContent}>
            <SimpleDuelPostImg
              userOneProfileImgSrc={detailProps.agent1.avatarImage}
              userOneCoverImgSrc={detailProps.agent1.bannerImage}
              userTwoProfileImgSrc={detailProps.agent2.avatarImage}
              userTwoCoverImgSrc={detailProps.agent2.bannerImage}
            />
            <DuelCardKPIDetail
              kpi={duel.kpi_name || "new"}
              duration={getDuelStatus(duel, flip) === 1 ? duration : "0"}
            />
            {(duel.isAccepted || duel.isComplete) && (
              <DuelCardScoreDetail
                status={getDuelStatus(duel, flip)}
                user={users[0]}
                opponent={users[1]}
                userOneProfileImgSrc={
                  users[0].entityId === agent1.entityId
                    ? detailProps.agent1.avatarImage
                    : detailProps.agent2.avatarImage
                }
                userTwoProfileImgSrc={
                  users[1].entityId === agent2.entityId
                    ? detailProps.agent2.avatarImage
                    : detailProps.agent1.avatarImage
                }
              />
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
          {isNewDuelAccept() && (
            <Box className={classes.duelFooter}>
              <Typography align="right">
                <ButtonAcceptDuel
                  handleOnClick={handleAccept}
                  disabled={buttonStatus}
                >
                  Accept
                </ButtonAcceptDuel>

                <ButtonAcceptDuel
                  handleOnClick={handleReject}
                  disabled={buttonStatus}
                >
                  Decline
                </ButtonAcceptDuel>
              </Typography>
            </Box>
          )}
        </Paper>
      </Grid>
    </>
  );
};

export default DuelCard;
