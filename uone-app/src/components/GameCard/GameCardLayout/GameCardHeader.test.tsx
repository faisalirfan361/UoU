import React from "react";
import { render, cleanup } from "@testing-library/react";
import { GameCardHeader } from "./index";

describe("GameCardHeader", () => {
  afterEach(cleanup);

  const onDelete = () => {
    console.log("Open Delete Modal");
  };

  const onEdit = () => {
    console.log("Open Edit Modal");
  };

  test("correctly renders the state of GameCardHeader", () => {
    const { getByText, queryByText } = render(
      <GameCardHeader
        title="title"
        status={2}
        coins={100}
        onDelete={onDelete}
        onEdit={onEdit}
      />
    );
    // Test the initial state of the props.
    expect(queryByText("onDelete")).toBeDefined();
    expect(queryByText("onEdit")).toBeDefined();
    expect(getByText("title")).toBeDefined();
    expect(queryByText("status")).toBeDefined();
    expect(queryByText("coins")).toBeDefined();
  });
});
