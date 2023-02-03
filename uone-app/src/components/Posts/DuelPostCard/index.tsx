import React, { FC, useMemo } from "react";
import DuelPostCardProps from "./types";
import { default as PostTitle } from "../PostComponents/PostTitle";
import { default as SimpleDuelDescription } from "../PostComponents/PostDescriptions/SimpleDuelDescription";
import { default as PostInteractionManager } from "../PostComponents/PostInteractionManager";
import SimpleDuelPostImg from "components/Posts/PostComponents/PostImageSection/SimpleDuelPostImg";
import { Box } from "@material-ui/core";
import useDuelPostCardStyles from "./style";
import _get from "lodash.get";
import config from "../../../config";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";

const DuelPostCard: FC<DuelPostCardProps> = ({ duel, post }) => {
  const classes = useDuelPostCardStyles();
  const { userId, bannerImages, avatarImages } = useRecoilValue(userAtom);
  const postDate = new Date(post.createdAt);
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

  const agent1 = duel.profiles.length > 0 ? duel.profiles[0] : {};
  const agent2 = duel.profiles.length > 1 ? duel.profiles[1] : {};

  const detailProps = {
    agent1: {
      name: `${agent1.firstName || ""} ${agent1.lastName || ""}`,
      avatarImage: getAvatarImage(agent1.entityId || ""),
      bannerImage: getBannerImage(agent1.entityId || ""),
    },
    agent2: {
      name: `${agent2.firstName || ""} ${agent2.lastName || ""}`,
      avatarImage: getAvatarImage(agent2.entityId || ""),
      bannerImage: getBannerImage(agent2.entityId || ""),
    },
  };

  return (
    <Box className={classes.root}>
      <PostTitle
        postTitle={"NEW AGENT DUEL"}
        postDate={postDate}
        iconType={"MOUNT"}
      />

      <SimpleDuelPostImg
        userOneProfileImgSrc={detailProps.agent1.avatarImage}
        userOneCoverImgSrc={detailProps.agent1.bannerImage}
        userTwoProfileImgSrc={detailProps.agent2.avatarImage}
        userTwoCoverImgSrc={detailProps.agent2.bannerImage}
      />
      <Box className={classes.boxHeight}></Box>
      <SimpleDuelDescription
        showDetails={true}
        postName="Dueled"
        userOne={detailProps.agent1.name}
        userTwo={detailProps.agent2.name}
        iconType={""}
        details={duel.title}
      />

      <PostInteractionManager post={post} />
    </Box>
  );
};

export default DuelPostCard;
