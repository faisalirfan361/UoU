import React, { FC } from "react";
import { Box, Grid, Typography } from "@material-ui/core";
import clsx from "clsx";
import TableCell from "@material-ui/core/TableCell";

import { ChallengeUserRowProps } from "./types";
import ZebraTableRow from "components/TableComponents/ZebraTableRow";
import useStyles from "./style";
import StyledAvatar from "components/StyledAvatar";

const ChallengeUserRow: FC<ChallengeUserRowProps> = ({ user, rowNumber }) => {
  const classes = useStyles();

  return (
    <ZebraTableRow className={clsx({ [classes.winnerRow]: user.isWinner })}>
      <TableCell className={classes.rankColumn}>{`${rowNumber}.`}</TableCell>
      <TableCell className={classes.agentNameColumn}>
        <Grid container spacing={0}>
          <Grid item>
            <StyledAvatar
              src={user.profileImg}
              className={`${classes.avatar}`}
            />
          </Grid>
          <Grid item>
            <Typography className={classes.agentName}>
              {`${user.firstName} ${user.lastName}`}
            </Typography>
          </Grid>
        </Grid>
      </TableCell>
      <TableCell className={classes.pointsColumn}>
        {`${user.pointsBalance ? user.pointsBalance : 0}`}
      </TableCell>
    </ZebraTableRow>
  );
};

export default ChallengeUserRow;
