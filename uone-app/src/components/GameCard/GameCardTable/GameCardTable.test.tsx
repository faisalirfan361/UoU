import { render, cleanup } from "@testing-library/react";
import React from "react";
import GameCardTable from "./GameCardTable";
import { humanReadableDecimal } from "utils/humanReadable";
import { GameRankUser } from "../type";
import GameCardAgentCell from "./GameCardAgentCell";
import { ColumnDefinition } from "./GameCardTable";
describe("GameCardTable", () => {
  afterEach(cleanup);

  const data: GameRankUser[] = [
    { rank: 1, fullName: "test user one", avatar: "test.jpg", score: 10 },
    { rank: 2, fullName: "test user two", avatar: "test1.jpg", score: 5 },
    { rank: 3, fullName: "test user three", avatar: "test2.jpg", score: 0 },
  ];

  const columns: ColumnDefinition[] = [
    { id: "rank", label: "Rank", className: "tableText" },
    {
      id: "fullName",
      label: "Agent Name",
      className: "tableText agentName",
      component: GameCardAgentCell,
    },
    {
      id: "score",
      label: "Score",
      className: "tableText",
      formatter: humanReadableDecimal,
    },
  ];

  test("correctly renders the state of GameCardTable", () => {
    const { getByText, queryByText } = render(
      <GameCardTable
        data={data}
        columns={columns}
        status={100}
        gameId="test-game-Id"
      />
    );
    // Test the initial state of the props.
    expect(queryByText("data")).toBeDefined();
    expect(queryByText("columns")).toBeDefined();
    expect(queryByText("status")).toBeDefined();
    expect(queryByText("gameId")).toBeDefined();
  });
});
