import React, { MouseEvent, memo, useEffect } from "react";
import Box from "@material-ui/core/Box";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import Divider from "@material-ui/core/Divider";
import Typography from "@material-ui/core/Typography";
import useSWR from "swr";
import { useRecoilValue } from "recoil";
import _get from "lodash.get";

import { getComparator, stableSort } from "utils/sort";

import { AvatarComponent } from "components";
import { userAtom } from "state";
import useStyle from "./styles";

const headCells = [
  {
    id: "agentName",
    numeric: false,
    disablePadding: true,
    label: "Agent Name",
  },
  {
    id: "teamName",
    numeric: false,
    disablePadding: false,
    label: "Team",
  },
  {
    id: "roleName",
    numeric: false,
    disablePadding: false,
    label: "Role",
  },
];

const EnhancedTableHead = (props: any) => {
  const { classes, order, orderBy, onRequestSort } = props;

  const createSortHandler =
    (property: any) => (event: React.MouseEvent<unknown>) => {
      onRequestSort(event, property);
    };

  return (
    <TableHead>
      <TableRow>
        {headCells.map((headCell) => (
          <TableCell
            key={headCell.id}
            align="left"
            padding="default"
            sortDirection={orderBy === headCell.id ? order : false}
          >
            <TableSortLabel
              active={orderBy === headCell.id}
              direction={orderBy === headCell.id ? order : "asc"}
              onClick={createSortHandler(headCell.id)}
            >
              <Typography
                className={classes.tableHeaderTitle}
                variant="subtitle2"
                gutterBottom
              >
                {headCell.label}
              </Typography>
              {orderBy === headCell.id ? (
                <span className={classes.visuallyHidden}>
                  {order === "desc" ? "sorted descending" : "sorted ascending"}
                </span>
              ) : null}
            </TableSortLabel>
          </TableCell>
        ))}
      </TableRow>
    </TableHead>
  );
};

let peopleAPIPayload = {
  path: "",
  method: "",
};

const TeamsPeople = (props: any) => {
  // props.showLayoutLoader();
  const classes = useStyle();
  const { clientId, departmentId } = useRecoilValue(userAtom);

  const [order, setOrder] = React.useState("asc");
  const [orderBy, setOrderBy] = React.useState("calories");

  peopleAPIPayload.path = `/entity/list-all-members`;
  peopleAPIPayload.method = "GET";

  const {
    data: { peopleData, count },
  } = useSWR([`${peopleAPIPayload.path}`, peopleAPIPayload], {
    suspense: false,
  });

  const handleRequestSort = (event: MouseEvent, property: any) => {
    const isAsc = orderBy === property && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(property);
  };

  useEffect(() => {
    if (peopleData) {
      // props.hideLayoutLoader();
    }
  });

  if (!peopleData) return null;

  const people = peopleData ? peopleData : [];

  return (
    <>
      <TableContainer>
        <Table size="medium">
          <TableHead>
            <TableRow>
              <TableCell>
                <Typography
                  className={classes.tableCountTitle}
                  variant="subtitle2"
                  gutterBottom
                >
                  {count} People
                </Typography>
              </TableCell>
              <TableCell></TableCell>
              <TableCell></TableCell>
            </TableRow>
          </TableHead>
          <EnhancedTableHead
            classes={classes}
            order={order}
            orderBy={orderBy}
            onRequestSort={handleRequestSort}
            rowCount={people.length}
          />
          <TableBody>
            {stableSort(people, getComparator(order, orderBy)).map(
              (row: any, index: any) => {
                const agent = {
                  name: row.agentName,
                  team: row.teamName,
                  role: row.roleName,
                };

                return (
                  <TableRow
                    key={agent.name + index}
                    classes={{
                      root: `${classes.row} ${
                        index % 2 ? classes.rowEven : classes.rowOdd
                      }`,
                    }}
                  >
                    <TableCell align="left" padding="default">
                      <Box display="flex" flexWrap="wrap" p={0} m={0}>
                        <Box p={0} m={0} alignSelf="center" alignItems="center">
                          <AvatarComponent />
                        </Box>
                        <Box
                          p={0}
                          m={1}
                          alignSelf="center"
                          alignItems="center"
                          flexGrow={1}
                        >
                          <Typography
                            className={classes.tableBodyTitle}
                            variant="subtitle2"
                            gutterBottom
                          >
                            {agent.name}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography
                        className={classes.tableBodySubtitle}
                        variant="subtitle2"
                        gutterBottom
                      >
                        {agent.team}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography
                        className={classes.tableBodySubtitle}
                        variant="subtitle2"
                        gutterBottom
                      >
                        {agent.role}
                      </Typography>
                    </TableCell>
                  </TableRow>
                );
              }
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <Divider classes={{ root: classes.tableDivider }} />
    </>
  );
};

export default memo(TeamsPeople);
