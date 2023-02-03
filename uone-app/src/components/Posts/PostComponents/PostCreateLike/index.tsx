import React, { FC, useEffect, useState } from "react";
import { useSnackbar } from "notistack";
import { ERROR_TOAST_OPTIONS } from "../../../../constants";
import { API } from "aws-amplify";
import config from "../../../../config";
import { useRecoilValue } from "recoil";
import { userAtom } from "../../../../state";
import ThumbUpAltOutlinedIcon from "@material-ui/icons/ThumbUpAltOutlined";
import ThumbUpAltIcon from "@material-ui/icons/ThumbUpAlt";

import IProps from "./types";
import useStyles from "./style";

const userHasLikedPost = function (userId: string, likes: any[]) {
  return likes.find((like) => like.userId == userId);
};

const PostCreateLike: FC<IProps> = ({
  post,
  likes,
  refreshLikes,
  parentClass,
}) => {
  const { userId, profileImg, fullName } = useRecoilValue(userAtom);
  const classes = useStyles();
  const [userHasLiked, setUserHasLiked] = useState(false);
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    setUserHasLiked(userHasLikedPost(userId, likes));
  }, []);
  const createLike = async function () {
    if (!userHasLiked) {
      try {
        setUserHasLiked(true);
        let likePayload;
        if (likes.length > 0) {
          likePayload = [
            ...likes,
            { userId: userId, avatarUrl: profileImg, name: fullName },
          ];
        } else {
          likePayload = [
            { userId: userId, avatarUrl: profileImg, name: fullName },
          ];
        }
        await API.post(config.apiGateway.NAME, `/feed/update`, {
          body: {
            post: {
              ...post,
              likes: likePayload,
            },
            islike: true,
            userId: userId,
          },
        });

        refreshLikes(likePayload);
      } catch (e) {
        enqueueSnackbar("Failed to create like", ERROR_TOAST_OPTIONS);
      }
    }
  };

  return (
    <div className={classes.root}>
      <button
        className={`${classes.likeBtn}`}
        onClick={() => {
          createLike();
        }}
      >
        {userHasLiked ? <ThumbUpAltIcon /> : <ThumbUpAltOutlinedIcon />}

        <span>Like</span>
      </button>
    </div>
  );
};

export default PostCreateLike;
