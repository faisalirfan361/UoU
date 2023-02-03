import React, { FC, useState } from "react";
import IProps from "./types";
import { Box, Grid } from "@material-ui/core";
import TextareaAutosize from "react-textarea-autosize";
import useStyles from "./style";
import TelegramIcon from "@material-ui/icons/Telegram";
import { useSnackbar } from "notistack";
import {
  ERROR_TOAST_OPTIONS,
  SUCCESS_TOAST_OPTIONS,
} from "../../../../constants";
import { API } from "aws-amplify";
import config from "../../../../config";
import { useRecoilValue } from "recoil";
import { userAtom } from "../../../../state";
import StyledAvatar from "components/StyledAvatar";

const PostCreateComment: FC<IProps> = ({ post, refreshComments, comments }) => {
  const { userId, fullName, avatarImages } = useRecoilValue(userAtom);
  const classes = useStyles();
  const [comment, setComment] = useState("");
  const [commentStatus, setCommentStatus] = useState(true);
  const [lockBtn, setLockBtn] = useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const avatar = `${config.targetBucketUrl}${avatarImages?.keys.medium}`;
  const createComment = async function () {
    if (!lockBtn) {
      try {
        setLockBtn(true);
        let commentPayload;

        if (comments.length > 0) {
          commentPayload = [
            ...comments,
            {
              userId: userId,
              avatar: avatar,
              name: fullName,
              comment: comment,
              date: new Date().toISOString(),
            },
          ];
        } else {
          commentPayload = [
            {
              userId: userId,
              avatar: avatar,
              name: fullName,
              comment: comment,
              date: new Date().toISOString(),
            },
          ];
        }
        await API.post(config.apiGateway.NAME, `/feed/update`, {
          body: {
            post: {
              ...post,
              comments: commentPayload,
            },
            isComment: true,
            userId: userId,
          },
        });
        setLockBtn(false);

        refreshComments(commentPayload);
        setComment("");
        setCommentStatus(false);
        enqueueSnackbar("Comment created successfully", SUCCESS_TOAST_OPTIONS);
      } catch (e) {
        enqueueSnackbar("Failed to create comment", ERROR_TOAST_OPTIONS);
        setLockBtn(false);
      }
    }
  };

  return (
    <>
      {commentStatus && (
        <Box className={classes.root}>
          <Grid container spacing={0}>
            <Grid className={classes.imgGrid}>
              <StyledAvatar src={avatar} className={classes.profileImg} />
            </Grid>
            <Grid
              className={`${classes.textAreaGrid} ${classes.textAreaContainer}`}
            >
              <TextareaAutosize
                className={classes.textArea}
                onChange={(event) => setComment(event.target.value)}
                minRows={1}
                maxRows={10}
                autoFocus
                id={`txt_${post.feedId}`}
                value={comment}
              />
              <div
                className={
                  comment
                    ? classes.iconSendContainer
                    : classes.iconSendContainerDisable
                }
                onClick={createComment}
              >
                <TelegramIcon />
              </div>
            </Grid>
          </Grid>
        </Box>
      )}
    </>
  );
};

export default PostCreateComment;
