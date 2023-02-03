import React, { FC, useState } from "react";
import IProps from "./types";
import { Box, Grid } from "@material-ui/core";
import useStyles from "./style";
import formatDistance from "date-fns/formatDistance";
import StyledAvatar from "components/StyledAvatar";
import config from "../../../../config";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";

const getFormatedDate = function (commentDate: string) {
  const today = new Date();
  const date = new Date(commentDate); //
  return formatDistance(date, today, {
    addSuffix: true,
  });
};

const PostComment: FC<IProps> = ({ comment }) => {
  const classes = useStyles();
  const { userId, avatarImages } = useRecoilValue(userAtom);
  const { name } = comment;
  const formatedDate: string = getFormatedDate(comment.date);

  const getAvatarImage = (imageUserId: string) => {
    let avatarUrl = `${config.targetBucketUrl}images/${imageUserId}/avatars/current.png`;
    if (userId == imageUserId) {
      avatarUrl = `${config.targetBucketUrl}${avatarImages?.keys.medium}`;
    }
    return avatarUrl;
  };

  const imgPath = getAvatarImage(comment.userId);

  return (
    <Box className={classes.root}>
      <Grid container spacing={0}>
        <Grid className={classes.imgGrid}>
          <StyledAvatar src={imgPath} className={classes.profileImg} />
        </Grid>
        <Grid className={classes.commentGrid}>
          <p className={classes.commentTitle}>
            <span className={classes.userName}>{`${name}`}</span>
            <span className={classes.date}>{formatedDate}</span>
          </p>
        </Grid>
      </Grid>
      <Grid container spacing={0}>
        <Grid className={classes.imgGrid}></Grid>
        <Grid className={classes.commentGrid}>
          <p className={classes.commentText}>{`${comment.comment}`}</p>
        </Grid>
      </Grid>
    </Box>
  );
};

export default PostComment;
