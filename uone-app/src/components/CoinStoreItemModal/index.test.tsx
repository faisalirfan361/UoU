import React, { useState } from "react";
import { render, cleanup } from "@testing-library/react";
import CoinStoreItemModal from "./index";
import { RecoilRoot } from "recoil";
import { SnackbarProvider } from "notistack";

describe("CoinStoreItemModal", () => {
  afterEach(cleanup);

  test("correctly renders the state of CoinStoreItemModal", () => {
    const { getByText, queryByText } = render(
      <RecoilRoot>
        <SnackbarProvider>
          <CoinStoreItemModal
            selectedItem="seletedItem"
            open={true}
            onClose={() => {
              console.log("Modal close call");
            }}
            onRedeem={() => {
              console.log("item redeemed successfully");
            }}
          />
        </SnackbarProvider>
      </RecoilRoot>
    );
    // Test the initial state of the props.
    expect(queryByText("selectedItem")).toBeDefined();
    expect(queryByText("open")).toBeDefined();
  });
});
