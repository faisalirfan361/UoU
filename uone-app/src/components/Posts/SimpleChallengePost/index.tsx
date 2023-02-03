import React, { FC } from "react";
import { Box } from "@material-ui/core";

import SimpleChallengePostProps from "./types";
import { default as PostTitle } from "../PostComponents/PostTitle";
import { default as ChallengeRankTable } from "../PostComponents/PostImageSection/ChallengeRankTable";
import { default as ChallengeDescription } from "../PostComponents/PostDescriptions/ChallengeDescription";
import { default as PostInteractionManager } from "../PostComponents/PostInteractionManager";
import useStyles from "./style";

//Method to mock the winner of the challenge
//If a challende end_date already pass, the challenge will be mtreated as done
//and make the fist user of the Users list as the winner with 100 points
//This needs to change onces the endpoint return the correct winner of the challenge
//Aramis 06/28/2021
const generateMockedWinner = (challenge: any) => {
  const challengeEndDate = new Date(challenge.end_date);
  const today = new Date();
  let mockedUsers = [...challenge.Users];

  if (challengeEndDate < today) {
    const winner = { ...challenge.Users[0], isWinner: true, points: 100 };
    mockedUsers.shift();
    mockedUsers.unshift(winner);
  }
  return mockedUsers;
};

const SimpleChallengePost: FC<SimpleChallengePostProps> = ({
  challenge,
  post,
}) => {
  const classes = useStyles();

  const postDate = new Date(challenge.start_date);
  const challengeEndDate = new Date(challenge.end_date);
  const users = generateMockedWinner(challenge);

  return (
    <Box className={classes.root}>
      <PostTitle
        postTitle={"AGENT CHALLENGE"}
        postDate={postDate}
        iconType={"FLAG"}
      />
      <ChallengeRankTable users={users} challengeId={challenge.challenge_id} />
      <ChallengeDescription
        kpiName={challenge.title}
        endDate={challengeEndDate}
      />
      <PostInteractionManager post={post} />
    </Box>
  );
};

export default SimpleChallengePost;
