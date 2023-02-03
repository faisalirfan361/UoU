import React, { useState, useEffect, memo } from "react";
import Grid from "@material-ui/core/Grid";
import { IProps } from "./types";
import InfiniteScroll from "react-infinite-scroll-component";
import useStyles from "./style";
import { POST_TYPES } from "../../../constants";
import { default as SimpleChallengePost } from "../../../components/Posts/SimpleChallengePost";
import { fetcher } from "../../../utils/fetcher";
import GoalCompletePost from "components/Posts/GoalCompletePost";
import DuelPostCard from "components/Posts/DuelPostCard";
import ChallengePostCard from "components/Posts/ChallengePostCard";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import { API } from "aws-amplify";
import config from "../../../config";
import _, { sortBy } from "lodash";

const postApiPayload = {
  path: "/post/posts",
  method: "GET",
};

const getPostComponentFromType = function (post: any, index: number): any {
  switch (post.postType) {
    case POST_TYPES.CHALLENGE_CREATED:
      return (
        <ChallengePostCard
          key={`post-item-${index}`}
          challenge={post.metaData}
          post={post}
        />
      );

    case POST_TYPES.DUEL_CREATED:
      return (
        <DuelPostCard
          key={`post-item-${index}`}
          duel={post.metaData}
          post={post}
        />
      );

    case POST_TYPES.GOAL_MET:
      return (
        <GoalCompletePost
          key={`post-item-${index}`}
          goal={post.metadata}
          post={post}
        />
      );
  }
};

const PostsList: React.FC<IProps> = (props: any) => {
  const classes = useStyles();

  const [limit] = useState(5);
  const [offSet, setOffset] = useState(0);
  const [hasMorePosts, setHasMorePosts] = useState(true);
  const [posts, setPosts] = useState([] as any);
  const { clientId, departmentId } = useRecoilValue(userAtom);

  useEffect(() => {
    getPosts();
  }, []);

  const getPosts = async () => {
    const res = await API.post(
      config.apiGateway.NAME,
      `/feed/get-by-client-id`,
      {
        body: {
          clientId: clientId,
        },
      }
    );
    if (res) {
      if (res.data.length > 0) {
        const data = sortBy(res.data, "createdAt").reverse();
        setPosts(data);
        setHasMorePosts(false);
      } else {
        setHasMorePosts(true);
      }
    }
  };

  const startLoading = () => {
    return <p style={{ textAlign: "center" }}>Loading...</p>;
  };
  return (
    <>
      <Grid container className={classes.root} direction="row">
        <Grid item xs={12}>
          <InfiniteScroll
            dataLength={posts.length}
            next={getPosts}
            hasMore={hasMorePosts}
            loader={startLoading()}
            scrollableTarget={"content"}
            endMessage={
              <p style={{ textAlign: "center" }}>
                There are no more posts to show right now
              </p>
            }
          >
            {posts
              ? posts.map((post: any, index: number) => {
                  return getPostComponentFromType(post, index);
                })
              : null}
          </InfiniteScroll>
        </Grid>
      </Grid>
    </>
  );
};

export default memo(PostsList);
