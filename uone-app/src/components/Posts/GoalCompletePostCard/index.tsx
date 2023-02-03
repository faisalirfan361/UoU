import React, { FC, useMemo } from "react";
import GoalCompletePostCardProps from "./types";
import { default as PostTitle } from "../PostComponents/PostTitle";
import { default as SinglePostImg } from "../PostComponents/PostImages/SinglePostImg";
import { default as SimpleDuelDescription } from "../PostComponents/PostDescriptions/SimpleDuelDescription";
import { default as PostInteractionManager } from "../PostComponents/PostInteractionManager";
import { Box } from "@material-ui/core";
import useGoalCompletePostCardStyles from "./style";
import _get from "lodash.get";

const randInt = (max: number) => {
  return Math.floor(Math.random() * max);
};

const getUserCoverImg = function (user: any) {
  //returns a default random cover img
  return `https://picsum.photos/${1000 + randInt(1000)}`;
};

const GoalCompletePostCard: FC<GoalCompletePostCardProps> = ({
  goal,
  post,
}) => {
  const classes = useGoalCompletePostCardStyles();
  const defaultUser = { firstName: "user", lastName: "" };

  const userOne = goal.Users[0] ? goal.Users[0] : defaultUser;
  const userTwo = goal.Users[1] ? goal.Users[1] : defaultUser;
  const postDate = new Date("2021-09-15 00:00:00");
  const challengeCustomStyleProps = {
    agent1: _get(
      goal.Users[0],
      "[0].backgroundImageUrl",
      `https://picsum.photos/${1000 + randInt(1000)}`
    ),
    agent2: _get(
      goal.Users[1],
      "[1].backgroundImageUrl",
      `https://picsum.photos/${1000 + randInt(1000)}`
    ),
  };

  return (
    <Box className={classes.root}>
      <PostTitle
        postTitle={"GOAL REACHED"}
        postDate={postDate}
        iconType={"MOUNT"}
      />

      <SinglePostImg
        userOneProfileImgSrc={challengeCustomStyleProps.agent1}
        userOneCoverImgSrc={challengeCustomStyleProps.agent1}
        userTwoProfileImgSrc={challengeCustomStyleProps.agent2}
        userTwoCoverImgSrc={challengeCustomStyleProps.agent2}
      />

      <SimpleDuelDescription
        showDetails={true}
        postName="reached"
        userOne={userOne}
        userTwo={userTwo}
        iconType={""}
        details={`${userOne.firstName} ${userOne.lastName} reached ${userTwo.firstName} ${userTwo.lastName}`}
      />

      <PostInteractionManager post={post} />
    </Box>
  );
};

export default GoalCompletePostCard;
