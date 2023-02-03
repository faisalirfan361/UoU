import React, { FC } from "react";
import { Box, Grid } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";

import { ChallengePostImgProps, User } from "./types";
import UOneTableHeader from "components/TableComponents/UOneTableHeader";
import ChallengeUserRow from "./ChallengeUserRow";
import ChallengeBlankRow from "./ChallengeBlankRow";
import useStyles from "./style";

const blankName = "blank";
const createBlackUser = (): User => {
  return {
    user_id: -1,
    firstName: blankName,
    lastName: "",
    points: "-",
    profileImg: "",
    isWinner: false,
  };
};

const formatUsersRows = (users: User[]) => {
  if (users.length < 5) {
    const newArr = new Array(5).fill(null);
    return newArr.map((_item, index) => users[index] || createBlackUser());
  } else {
    return users;
  }
};

const ChallengeRankTable: FC<ChallengePostImgProps> = ({
  users,
  challengeId,
}) => {
  const classes = useStyles();
  //forces table to have at least 5 rows
  const usersRows = formatUsersRows(users);

  return (
    <Box className={classes.root}>
      <Grid container direction="row" spacing={0}>
        <Grid item className={classes.itemContainer}>
          <TableContainer className={classes.tableContainer}>
            <Table
              className={classes.table}
              stickyHeader
              aria-label="sticky table"
            >
              <TableHead>
                <TableRow>
                  <UOneTableHeader>RANK</UOneTableHeader>
                  <UOneTableHeader>AGENT NAME</UOneTableHeader>
                  <UOneTableHeader>POINTS</UOneTableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {usersRows.map((user: User, index: number) =>
                  user.firstName != blankName ? (
                    <ChallengeUserRow
                      user={user}
                      rowNumber={index + 1}
                      key={`challenge-user-row-${challengeId}-${index}`}
                    />
                  ) : (
                    <ChallengeBlankRow
                      rowNumber={index + 1}
                      key={`challenge-blank-row-${challengeId}-${index}`}
                    />
                  )
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ChallengeRankTable;
