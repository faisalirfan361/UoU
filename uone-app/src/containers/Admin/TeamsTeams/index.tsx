import React, { MouseEvent, memo, useEffect, useRef } from "react";
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
import CircularProgress from "@material-ui/core/CircularProgress";
import useSWR from "swr";
import { API } from "aws-amplify";
import InfiniteScroll from "react-infinite-scroll-component";
import config from "config";
import { useRecoilValue } from "recoil";
import _get from "lodash.get";

import { getComparator, stableSort } from "utils/sort";

import { GroupedAvatarsComponent } from "components";

import { userAtom } from "state";

import useStyle from "./styles";
import DeptNameEdit from "components/DepartmentNameEdit";
import StyledAvatar from "components/StyledAvatar";

const headCells = [
  {
    id: "teamAvatar", // Team Name
    numeric: false,
    disablePadding: true,
    label: "",
  },
  {
    id: "teamName", // Team Name
    numeric: false,
    disablePadding: true,
    label: "Team Name",
  },
  {
    id: "fullName", // Team Lead
    numeric: false,
    disablePadding: false,
    label: "Team Lead",
  },
  {
    id: "size", // Number of Members
    numeric: true,
    disablePadding: false,
    label: "Number of Members",
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

let teamsAPIPayload = {
  path: "",
  method: "",
};

const TeamsTeams = (props: any) => {
  // props.showLayoutLoader();

  const classes = useStyle();
  const { clientId } = useRecoilValue(userAtom);

  const [order, setOrder] = React.useState("asc");
  const [orderBy, setOrderBy] = React.useState("calories");
  const [teams, setTeams] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [page, setPage] = React.useState(0);
  const [count, setCount] = React.useState(0);
  const myRef = useRef<HTMLDivElement>(null);
  const [distanceBottom, setDistanceBottom] = React.useState(0);
  //const [hasMore, setHasMore] = React.useState(true);

  const getTeamsData = async (pageNum?: number) => {
    teamsAPIPayload.path = `/entity/get-teams-summary`;
    if (pageNum) {
      teamsAPIPayload.path = teamsAPIPayload.path + `?lastKey=${pageNum}`;
    }
    try {
      const result: any = await API.get(
        config.apiGateway.NAME,
        teamsAPIPayload.path,
        {}
      );
      setTeams((prev) => prev.concat(result.data || []));
      setCount(result.count);
    } catch (error) {
      console.error("Failed to fetch teams", error);
    }
  };
  useEffect(() => {
    setLoading(true);
    Promise.all([getTeamsData()])
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  const handleRequestSort = (event: MouseEvent, property: any) => {
    const isAsc = orderBy === property && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(property);
  };

  const hasMore = (teams.length && teams.length % 10 === 0) as boolean;
  const loadMore = async () => {
    setLoading(true);
    await Promise.all([getTeamsData(page + 1)]);
    if (teams.length % 10 !== 0) {
      setLoading(false);
    } else {
      setLoading(true);
    }
    setPage(page + 1);
  };

  const Loading = (loading: boolean) =>
    loading ? (
      <div className={classes.loading}>
        <CircularProgress className={classes.CircularProgress} />
      </div>
    ) : (
      <></>
    );
  return (
    <>
      <InfiniteScroll
        next={loadMore}
        hasMore={hasMore}
        dataLength={count}
        loader={Loading(loading)}
        scrollableTarget={myRef}
      >
        <TableContainer ref={myRef}>
          <Table size="medium">
            <TableHead>
              <TableRow>
                <TableCell>
                  <Typography
                    className={classes.tableCountTitle}
                    variant="subtitle2"
                    gutterBottom
                  >
                    {count} Teams
                  </Typography>
                </TableCell>
                <TableCell></TableCell>
                <TableCell align="right">
                  {/* <ButtonActionComponent
                  handleOnClick={() =>
                    console.log("ButtonActionComponent Clicked!")
                  }
                >
                  Create New Team
                </ButtonActionComponent> */}
                </TableCell>
              </TableRow>
            </TableHead>
            <EnhancedTableHead
              classes={classes}
              order={order}
              orderBy={orderBy}
              onRequestSort={handleRequestSort}
              rowCount={count || 0}
            />
            <TableBody>
              {stableSort(teams || [], getComparator(order, orderBy)).map(
                (row: any, index: any) => {
                  const team = {
                    name: row.mask?.dname
                      ? row.mask?.dname
                      : row.attributes.dname,
                    lead: `${row.manager_first_name} ${row.manager_last_name}`,
                    size: row.user_count,
                    deptId: row.department_id,
                    department: row,
                  };
                  return (
                    <TableRow
                      key={team.name + index}
                      classes={{
                        root: `${classes.row} ${
                          index % 2 ? classes.rowEven : classes.rowOdd
                        }`,
                      }}
                    >
                      <TableCell align="left" padding="default" width="150px">
                        <Box className={classes.avatarBox}>
                          <StyledAvatar src={row.profileImg} />
                        </Box>
                      </TableCell>
                      <TableCell align="left" padding="default" width="50%">
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
                            <DeptNameEdit department={team.department} />
                          </Box>
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          className={classes.tableBodySubtitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          {team.lead}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          className={classes.tableBodySubtitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          {team.size}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  );
                }
              )}
            </TableBody>
          </Table>
        </TableContainer>
        {loading && Loading}
        <Divider classes={{ root: classes.tableDivider }} />
      </InfiniteScroll>
    </>
  );
};

export default memo(TeamsTeams);
