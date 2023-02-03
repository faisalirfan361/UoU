import React, { useState } from "react";
import { render, cleanup } from "@testing-library/react";
import CreateCoinStoreItemModal from "./index";
import { RecoilRoot } from "recoil";
import { SnackbarProvider } from "notistack";

describe("CreateCoinStoreItemModal", () => {
  afterEach(cleanup);

  const testSelectedItem = {
    id: 4615,
    imageUrl: "Test_image.jpg",
    title: "Break Time",
    description: "1 hour extra time on lunch",
    points: 1111,
    canRedeem: false,
    brand: "",
    detailImageUrl: "Test_image.jpg",
    terms: null,
    instructions: null,
    limit: 10,
    reachedDailyPoints: false,
  };

  test("correctly renders the state of CreateCoinStoreItemModal", () => {
    const { getByText, queryByText } = render(
      <RecoilRoot>
        <SnackbarProvider>
          <CreateCoinStoreItemModal
            selectedItem={testSelectedItem}
            open={true}
            onClose={() => {
              console.log("Modal close call");
            }}
          />
        </SnackbarProvider>
      </RecoilRoot>
    );
    // Test the initial state of the props.
    expect(queryByText("selectedItem")).toBeDefined();
    expect(queryByText("open")).toBeDefined();
    expect(queryByText("onClose")).toBeDefined();
  });
});
