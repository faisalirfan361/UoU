import {
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@material-ui/core";
import { FC, useMemo, useRef, useState } from "react";
import _uniqueId from "lodash/uniqueId";
import React from "react";

import { GameRankUser } from "../type";
import GameCardEmptyRow from "./GameCardEmptyRow";
import useTableStyles from "./styles";
import InfiniteScroll from "react-infinite-scroll-component";

export interface ColumnDefinition {
  id: "rank" | "fullName" | "score";
  label: string;
  className?: string;
  component?: React.ElementType<any>;
  formatter?(value: any): string;
}

export interface GameCardTableProps {
  data: GameRankUser[];
  columns: ColumnDefinition[];
  status: number;
  gameId: string;
}

const GameCardTable: FC<GameCardTableProps> = ({
  data,
  columns,
  status,
  gameId,
}) => {
  const tableClasses = useTableStyles();
  const challengeElementId = useMemo(() => _uniqueId("challenge-"), []);
  const challengePlaceholderId = useMemo(() => _uniqueId("challenge-"), []);
  const [rowList, setRowList] = useState(data.slice(0, 10));
  const [lastObjectPosition, setLastObjectPosition] = useState(10);
  const limit = 10;

  const loadMore = () => {
    setRowList((prev) =>
      prev.concat(data.slice(lastObjectPosition, lastObjectPosition + limit))
    );
    setLastObjectPosition((currentValue) => {
      return currentValue + limit;
    });
  };

  /**
   * this function is to get custom styles for winner row
   * @param index is the rank of the agent
   * @param component is to check if it is call from component
   * @returns return the style of the row
   */
  const getWinnerStyle = (index: number, component?: boolean) => {
    if (component) {
      return {
        color: index === 0 && status === 3 ? "white" : "#2FB0D9",
      };
    }
    return {
      background: index === 0 && status === 3 ? "#2FB0D9" : "white",
      color: index === 0 && status === 3 ? "white" : "black",
    };
  };

  const rowLoader = () => {
    return <GameCardEmptyRow />;
  };

  return (
    <InfiniteScroll
      dataLength={rowList.length}
      next={loadMore}
      hasMore={lastObjectPosition < data.length}
      loader={rowLoader}
      scrollableTarget={`id-${gameId}`}
    >
      <TableContainer className={tableClasses.container} id={`id-${gameId}`}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={column.id}
                  className={tableClasses.stickyHeader}
                >
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>

          <TableBody>
            {rowList.map((row: GameRankUser, index) => {
              if (row) {
                return (
                  <TableRow
                    hover
                    role="checkbox"
                    tabIndex={-1}
                    key={`${challengeElementId}-rank-${index}`}
                    style={{ ...getWinnerStyle(index) }}
                  >
                    {columns.map((column) => {
                      const value = row[column.id];

                      if (column.component)
                        return (
                          <column.component
                            key={column.id}
                            className={column.className}
                            row={row}
                            style={getWinnerStyle(index, true)}
                          />
                        );
                      return (
                        <TableCell
                          key={column.id}
                          className={column.className}
                          style={{ ...getWinnerStyle(index) }}
                        >
                          {column.formatter ? column.formatter(value) : value}
                        </TableCell>
                      );
                    })}
                  </TableRow>
                );
              }
              return (
                <GameCardEmptyRow
                  key={`${challengePlaceholderId}-rank-${index}`}
                />
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </InfiniteScroll>
  );
};

export default GameCardTable;
