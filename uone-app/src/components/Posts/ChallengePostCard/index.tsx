import React, { FC, useEffect, useMemo, useState } from "react";
import ChallengePostCardProps from "./types";
import { default as PostTitle } from "../PostComponents/PostTitle";
import { default as SimpleChallengeDescription } from "../PostComponents/PostDescriptions/SimpleChallengeDescription";
import { default as PostInteractionManager } from "../PostComponents/PostInteractionManager";
import { Box } from "@material-ui/core";
import useChallengePostCardStyles from "./style";
import _get from "lodash.get";
import GameCard from "components/GameCard";

const randInt = (max: number) => {
  return Math.floor(Math.random() * max);
};

const ChallengePostCard: FC<ChallengePostCardProps> = ({ challenge, post }) => {
  const classes = useChallengePostCardStyles();
  const [usersList, setUsersList] = useState("");
  const postDate = new Date(post.createdAt);

  useEffect(() => {
    handleShowParticipants();
  }, []);
  const handleShowParticipants = () => {
    let participantsData: any[] = [];
    if (challenge.profiles.length > 7) {
      for (let i = 0; i < 7; i++) {
        participantsData.push(
          ` ${challenge.profiles[i].firstName} ${challenge.profiles[i].lastName}`
        );
      }
      participantsData.push(`...`);
    } else {
      for (let i = 0; i < challenge.profiles.length; i++) {
        participantsData.push(
          ` ${challenge.profiles[i].firstName} ${challenge.profiles[i].lastName}`
        );
      }
    }
    setUsersList(
      `Participants:${participantsData.map((user: any, index: number) => {
        return `${user}`;
      })}`
    );
  };
  return (
    <Box className={classes.root}>
      <PostTitle
        postTitle={"New Challenge"}
        postDate={postDate}
        iconType={"MOUNT"}
      />
      <GameCard.GameCardMedia title={challenge.title} />
      <Box className={classes.boxHeight}></Box>
      <SimpleChallengeDescription iconType={""} details={usersList} />

      <PostInteractionManager post={post} />
    </Box>
  );
};

export default ChallengePostCard;
