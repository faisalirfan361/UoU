import React, { FC, useState } from "react";

import IProps from "./types";

import { Box, Link, Avatar, Typography } from "@material-ui/core";

/**
 * Store
 */
import { kpiAtom } from "state";
import { useRecoilState } from "recoil";

// import Chip from "@material-ui/core/Chip";

// import ButtonDeleteChallenge from '../ButtonDeleteChallenge'

import { DialogParticipants } from "components";
import useStyles from "./style";
import IKpiAtomState from "state/kpi/types";
import StyledAvatar from "components/StyledAvatar";

const BoxChallenge: FC<IProps> = ({ challenge }) => {
  const classes = useStyles();
  const [kpis] = useRecoilState(kpiAtom);
  const [open, setOpen] = useState(false);

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  const kpiTitle = (challenge: any) => {
    const { key } =
      kpis?.find(({ cognitoID }) => challenge.metric === cognitoID) ||
      ({} as IKpiAtomState);
    return `${key}`;
  };

  // const handleClick = () => {};

  return (
    <Box
      className={classes.root}
      display="flex"
      flexWrap="wrap"
      alignContent="flex-start"
    >
      <StyledAvatar
        src={challenge.thumbnailImage}
        className={classes.thumbnail}
      />
      <Box
        display="flex"
        flex="1"
        flexDirection="column"
        alignItems="flex-start"
        ml={2}
        className={classes.challengeDetails}
      >
        <Box
          display="flex"
          flexDirection="row"
          justifyContent="space-between"
          width="100%"
        >
          <Typography className={classes.title}>{challenge.title}</Typography>
          {/* <ButtonDeleteChallenge challenge={challenge} /> */}
          {/* <Chip size="small" label="Join" onClick={handleClick}/> */}
        </Box>
        <Typography className={classes.metric}>
          {kpiTitle(challenge)}
        </Typography>
        <Typography className={classes.description}>
          {challenge.description}
        </Typography>
        <Typography
          className={classes.details}
        >{`Winner Points: ${challenge.winnerPoints}`}</Typography>
        <Typography className={classes.details}>{`When: ${new Date(
          challenge.start_date
        ).toLocaleDateString()} - ${new Date(
          challenge.end_date
        ).toLocaleDateString()}`}</Typography>
        {challenge?.agents?.length > 0 && (
          <Link href="#" onClick={handleClickOpen} color={"secondary"}>
            List of participants
          </Link>
        )}
      </Box>
      <DialogParticipants
        participants={challenge.agents}
        open={open}
        onClose={handleClose}
      />
    </Box>
  );
};

export default BoxChallenge;
