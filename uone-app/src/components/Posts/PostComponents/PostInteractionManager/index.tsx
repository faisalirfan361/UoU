import React, { FC, useEffect, useState } from "react";
import IProps from "./types";
import { Box, Grid } from "@material-ui/core";
import useStyles from "./style";
import ChatBubbleOutlineIcon from "@material-ui/icons/ChatBubbleOutline";
import ThumbUpAltOutlinedIcon from "@material-ui/icons/ThumbUpAltOutlined";
import PostCreateComment from "../PostCreateComment";
import PostComment from "../PostComment";
import PostCreateLike from "../PostCreateLike";
import _, { sortBy } from "lodash";

const PostInteractionManager: FC<IProps> = ({ post }) => {
  const classes = useStyles();

  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState(post.comments);
  const [likes, setLikes] = useState(post.likes);

  const refreshComments = async function (commentsResponse: any) {    
    setComments(commentsResponse);
  };

  const refreshLikes = async function (likeResponse: any) {
    setLikes(likeResponse);
  };

  return (
    <Box className={classes.root}>
      <Grid container spacing={0} className={classes.container}>
        <Grid item xs={6} className={classes.infoContainer}>
          <div className={classes.flexWrapperInfo}>
            <div className={classes.wrapperItemInfo}>
              <div className={"icon-like"}>
                <ThumbUpAltOutlinedIcon />
              </div>
              <div className={"count"}>
                <span>{likes.length}</span>
              </div>
            </div>
            <div className={classes.wrapperItemInfo}>
              <div className={"icon-comment"}>
                <ChatBubbleOutlineIcon />
              </div>
              <div className={"count"}>
                <span>{comments.length}</span>
              </div>
            </div>
          </div>
        </Grid>

        <Grid item xs={6} className={classes.actionsContainer}>
          <div className={classes.flexWrapperActions}>
            <PostCreateLike
              likes={likes}
              post={post}
              refreshLikes={refreshLikes}
              parentClass={`${classes.wrapperItemActions}`}
            />

            <div
              className={classes.wrapperItemActions}
              onClick={() => setShowComments(!showComments)}
            >
              <div className={"icon-comment"}>
                <ChatBubbleOutlineIcon />
              </div>
              <div className={"label"}>
                <span>Comment</span>
              </div>
            </div>
          </div>
        </Grid>
      </Grid>

      {showComments ? (
        <Grid item xs={12} className={classes.commentsContainer}>
          <PostCreateComment post={post} refreshComments={refreshComments} comments={comments}/>
          <div>
            {sortBy(comments, "date").reverse().map((comment: any, index: number) => {
              return <PostComment key={index} comment={comment} />;
            })}
          </div>
        </Grid>
      ) : (
        ""
      )}
    </Box>
  );
};

export default PostInteractionManager;
