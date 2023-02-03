import React, { FC } from "react";
import { Box, Grid, Typography, Button, IconButton } from "@material-ui/core";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";

import { Can } from "context/Ability/Can";
import { isDayAfter } from "../../../../utils/dateUtils";
import { ChallengeStates } from "../../../../constants";
import { getLongFormattedDateTime } from "../../../../utils/getStandardFormattedDateTime";
import { ChallengeTitleProps } from "./types";
import useChallengeTitleStyles from "./style";

const calculateChallengeState = (startDate: Date, endDate: Date): string => {
  const today = new Date();
  if (endDate < today) {
    return ChallengeStates.EXPIRED;
  } else if (today < startDate) {
    return ChallengeStates.FUTURE_START;
  } else {
    return ChallengeStates.ACTIVE;
  }
};

const canChallengeBeEdited = (endDate: Date) => {
  return isDayAfter(endDate, new Date());
};

const ChallengeTitle: FC<ChallengeTitleProps> = ({
  challengeId,
  challengeName,
  coins,
  startDate,
  endDate,
  editFunction,
  deleteFunction,
}) => {
  const classes = useChallengeTitleStyles();

  const state: string = calculateChallengeState(startDate, endDate);

  return (
    <Box>
      <Grid container direction="row" alignItems="center">
        <Grid item xs={10}>
          <Typography className={classes.challengeName}>
            {challengeName}
          </Typography>
        </Grid>
        <Grid item xs={2}>
          <Can I="delete" a="challenges">
            {deleteFunction ? (
              <IconButton
                aria-label="delete"
                className={classes.deleteIcon}
                onClick={() => {
                  deleteFunction(challengeId);
                }}
              >
                <DeleteIcon />
              </IconButton>
            ) : (
              ""
            )}
          </Can>
          <Can I="edit" a="challenges">
            {canChallengeBeEdited(endDate) && editFunction ? (
              <IconButton
                aria-label="delete"
                className={classes.editIcon}
                onClick={() => {
                  editFunction(challengeId);
                }}
              >
                <EditIcon />
              </IconButton>
            ) : (
              ""
            )}
          </Can>
        </Grid>
        <Grid item xs={6}>
          {state === ChallengeStates.FUTURE_START ? (
            <span className={classes.futureStart}>
              {`Set to start: ${getLongFormattedDateTime(startDate)}`}
            </span>
          ) : state === ChallengeStates.ACTIVE ? (
            <span className={classes.activeState}>{state}</span>
          ) : (
            <span className={classes.expiredState}>{state}</span>
          )}
        </Grid>
        <Grid item xs={6}>
          <Typography
            className={classes.coins}
            align="right"
          >{`${coins} Coins`}</Typography>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ChallengeTitle;
