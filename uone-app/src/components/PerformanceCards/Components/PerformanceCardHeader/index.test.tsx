import * as React from "react";
import { render, cleanup } from "@testing-library/react";
import PerformanceCardHeader from "./index";

describe("PerformanceCardHeader", () => {
  afterEach(cleanup);

  test("correctly renders the state of PerformanceCardHeader", () => {
    const { getByText, queryByText } = render(
      <PerformanceCardHeader
        children="children"
        status="status"
        statusColor="statusColor"
        mainText="mainText"
        secondaryText="secondaryText"
        titlesCss="titlesCss"
        deptText="deptText"
      />
    );
    // Test the initial state of the props.
    expect(getByText("children")).toBeDefined();
    expect(getByText("status")).toBeDefined();
    expect(getByText("mainText")).toBeDefined();
    expect(getByText("secondaryText")).toBeDefined();
    expect(queryByText("titlesCss")).toBeDefined();
    expect(queryByText("deptText")).toBeDefined();
  });
});
