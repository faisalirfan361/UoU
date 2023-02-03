import React, { useState } from "react";
import { render, cleanup } from "@testing-library/react";
import PostCreateComment from "./index";
import { RecoilRoot } from "recoil";
import { SnackbarProvider } from "notistack";

describe("PostCreateComment", () => {
  afterEach(cleanup);

  const post = {
    likes: [],
    updatedAt: "2022-06-21T08:00:44.465Z",
    groupId: "test-group-default",
    postType: "CHALLENGE_CREATED",
    comments: [],
    createdAt: "2022-06-21T08:00:44.465Z",
    feedId: "test-ab47-4cde-bf16-38da8e131b97",
    clientId: "test-client",
  };

  test("correctly renders the state of PostCreateComment", () => {
    const { getByText, queryByText } = render(
      <RecoilRoot>
        <SnackbarProvider>
          <PostCreateComment
            post={post}
            refreshComments={() => console.log("refresh comments")}
            comments="comment"
          />
        </SnackbarProvider>
      </RecoilRoot>
    );
    // Test the initial state of the props.
    expect(queryByText("post")).toBeDefined();
    expect(queryByText("comments")).toBeDefined();
    expect(queryByText("refreshComments")).toBeDefined();
  });
});
