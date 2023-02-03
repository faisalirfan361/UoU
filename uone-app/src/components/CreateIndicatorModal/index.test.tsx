import React from "react";
import { render, cleanup } from "@testing-library/react";
import CreateIndicatorModal from "./index";
import { RecoilRoot } from "recoil";
import { SnackbarProvider } from "notistack";

describe("CreateIndicatorModal", () => {
  afterEach(cleanup);

  test("correctly renders the state of CreateIndicatorModal", () => {
    const { getByText, queryByText } = render(
      <RecoilRoot>
        <SnackbarProvider>
          <CreateIndicatorModal
            departmentId="test-group-2001"
            open={true}
            onClose={() => {
              console.log("Modal close call");
            }}
            callback={() => {
              console.log("Modal callback function");
            }}
          />
        </SnackbarProvider>
      </RecoilRoot>
    );
    // Test the initial state of the props.
    expect(queryByText("departmentId")).toBeDefined();
    expect(queryByText("open")).toBeDefined();
    expect(queryByText("onClose")).toBeDefined();
    expect(queryByText("callback")).toBeDefined();
  });
});
