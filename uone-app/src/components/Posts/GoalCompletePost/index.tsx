import React, { FC, useMemo } from "react";
import GoalCompletePostProps from "./types";
import { default as PostTitle } from "../PostComponents/PostTitle";
import { default as SinglePostImg } from "../PostComponents/PostImages/SinglePostImg";
import { default as SimpleDuelDescription } from "../PostComponents/PostDescriptions/SimpleDuelDescription";
import { default as PostInteractionManager } from "../PostComponents/PostInteractionManager";
import { Avatar, Box, Typography } from "@material-ui/core";
import useStyles from "./style";
import config from "../../../config";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";

const randInt = (max: number) => {
  return Math.floor(Math.random() * max);
};

const GoalCompletePost: FC<GoalCompletePostProps> = ({ goal, post }) => {
  const classes = useStyles();
  const { firstName, avatarImages, bannerImages, userId } =
    useRecoilValue(userAtom);

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
      avatarUrl = `${config.targetBucketUrl}${bannerImages?.keys.medium}`;
    }
    return avatarUrl;
  };

  //Default user is used because create goal doesnt have validations that
  //should allow us to create a goal with at least two users
  const defaultUser = { firstName: "user", lastName: "" };

  //This needs to change onces the requirements for the goal are set with the
  //designer
  //-Aramis 05-11-2021
  const userOne = goal.Users[0] ? goal.Users[0] : defaultUser;
  const userTwo = goal.Users[1] ? goal.Users[1] : defaultUser;
  const postDate = new Date(goal.start_date);

  const coverImageUserOne = useMemo(() => getBannerImage(userOne.user_id), []);
  const coverImageUserTwo = useMemo(() => getBannerImage(userTwo.user_id), []);

  return (
    <Box className={classes.root}>
      <PostTitle
        postTitle={"GOAL REACHED"}
        postDate={postDate}
        iconType={"MOUNT"}
      />

      <SinglePostImg
        userOneProfileImgSrc={getAvatarImage(userOne.user_id)}
        userOneCoverImgSrc={coverImageUserOne}
        userTwoProfileImgSrc={getAvatarImage(userTwo.user_id)}
        userTwoCoverImgSrc={coverImageUserTwo}
      />

      <SimpleDuelDescription
        showDetails={false}
        postName="reached"
        userOne={userOne}
        userTwo={userTwo}
        iconType={""}
      />

      <PostInteractionManager post={post} />
    </Box>
  );
};

export default GoalCompletePost;
