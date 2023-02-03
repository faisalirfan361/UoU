//Hard code 3 departments in the Departments tab for Organization.
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

import _get from "lodash.get";
import MeetingRoomSharpIcon from "@material-ui/icons/MeetingRoomSharp";

import { getComparator, stableSort } from "utils/sort";

import useStyle from "./styles";
import useSWR from "swr";
import DeptNameEdit from "components/DepartmentNameEdit";
import { useDepartments } from "../../../hooks/useDepartments";

const headCells = [
  {
    id: "name", // Department Name
    numeric: false,
    disablePadding: true,
    label: "Department Name",
  },
  {
    id: "numberofTeams", // Number of teams
    numeric: true,
    disablePadding: false,
    label: "Number of Teams",
  },
  {
    id: "numberofPeoples", // Number of Peoples
    numeric: true,
    disablePadding: false,
    label: "Number of People",
  },
];

let teamsAPIPayload = {
  path: "",
  method: "",
};

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

const TeamsDepartment = (props: any) => {
  // props.showLayoutLoader();

  const classes = useStyle();

  const [order, setOrder] = React.useState("asc");
  const [orderBy, setOrderBy] = React.useState("calories");

  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();

  const handleRequestSort = (event: MouseEvent, property: any) => {
    const isAsc = orderBy === property && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(property);
  };
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
                  {departments?.length} Departments
                </Typography>
              </TableCell>
              <TableCell></TableCell>
              <TableCell align="right"></TableCell>
            </TableRow>
          </TableHead>
          <EnhancedTableHead
            classes={classes}
            order={order}
            orderBy={orderBy}
            onRequestSort={handleRequestSort}
            rowCount={departments?.length}
          />
          <TableBody>
            {departments &&
              stableSort(departments, getComparator(order, orderBy)).map(
                (row: any, index: any) => {
                  const department = {
                    department_id: row.entityId,
                    name:
                      (row.mask?.dname
                        ? row.mask?.dname
                        : row.attributes.dname) || "",
                    numberofTeams: row.group_count || 0,
                    numberofPeoples: row.user_count || 0,
                    department: row,
                  };
                  return (
                    <TableRow
                      key={department.name + index}
                      classes={{
                        root: `${classes.row} ${
                          index % 2 ? classes.rowEven : classes.rowOdd
                        }`,
                      }}
                    >
                      <TableCell align="left" padding="default">
                        <Typography
                          className={classes.tableBodyTitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          <Box
                            display="flex"
                            flexWrap="wrap"
                            p={0}
                            m={0}
                            alignItems="center"
                          >
                            <Box
                              p={0}
                              m={0}
                              alignSelf="center"
                              alignItems="center"
                              className={classes.boxGroupedAvatars}
                            >
                              <Box className={classes.iconWrapper}>
                                <MeetingRoomSharpIcon
                                  fontSize="large"
                                  className={classes.icon}
                                />
                              </Box>
                            </Box>
                            <Box
                              p={0}
                              m={0}
                              alignSelf="center"
                              alignItems="center"
                              flexGrow={1}
                            >
                              {department.department && (
                                <DeptNameEdit
                                  department={department.department}
                                />
                              )}
                            </Box>
                          </Box>
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          className={classes.tableBodySubtitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          {department.numberofTeams}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          className={classes.tableBodySubtitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          {department.numberofPeoples}
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

export default memo(TeamsDepartment);
