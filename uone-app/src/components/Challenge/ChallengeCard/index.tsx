import React, { FC } from "react";
import { Box } from "@material-ui/core";

import ChallengeTitle from "../Components/ChallengeTitle";
import ChallengeRankTable from "components/Posts/PostComponents/PostImageSection/ChallengeRankTable";
import ChallengeDescription from "components/Posts/PostComponents/PostDescriptions/ChallengeDescription";
import useChallengeCardStyles from "./style";
import { ChallengeCardProps, User } from "./types";

//Method to mock the winner of the challenge
//If a challenge end_date already pass, the challenge will be treated as done
//and make the fist user of the Users list as the winner with 100 points
//This needs to change onces the endpoint return the correct winner of the challenge
//Aramis 07/12/2021
const generateMockedWinner = (challenge: any): User[] => {
  const challengeEndDate = new Date(challenge.end_ate);
  const today = new Date();
  let mockedUsers: User[] = [];

  if (challenge.profiles.users) {
    mockedUsers = [...challenge.profiles.users];

    if (challengeEndDate < today && challenge.profiles.users.length > 0) {
      const winner = {
        ...challenge.profiles.users[0],
        isWinner: true,
        points: 100,
      };
      mockedUsers.shift();
      mockedUsers.unshift(winner);
    }
  }

  return mockedUsers;
};

const ChallengeCard: FC<ChallengeCardProps> = ({
  challenge,
  editFunction,
  deleteFunction,
}) => {
  const classes = useChallengeCardStyles();

  const challengeStartDate = new Date(challenge.start_date);
  const challengeEndDate = new Date(challenge.end_date);
  const users = generateMockedWinner(challenge);

  return (
    <Box
      className={classes.root}
      key={`challenge-card-${challenge.challengeId}`}
    >
      <ChallengeTitle
        challengeId={challenge.gameId}
        challengeName={challenge.title}
        startDate={challengeStartDate}
        endDate={challengeEndDate}
        coins={challenge.winnerPoints}
        editFunction={editFunction}
        deleteFunction={deleteFunction}
      />
      <ChallengeRankTable users={users} challengeId={challenge.gameId} />
      <ChallengeDescription
        kpiName={challenge.title}
        endDate={challengeEndDate}
      />
    </Box>
  );
};

export default ChallengeCard;
