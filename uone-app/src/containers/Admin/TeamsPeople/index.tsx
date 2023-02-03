import React, {
  MouseEvent,
  memo,
  useEffect,
  useLayoutEffect,
  useCallback,
  useRef,
  useMemo,
} from "react";
import { Box, Tooltip } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import Divider from "@material-ui/core/Divider";
import Typography from "@material-ui/core/Typography";
import { API } from "aws-amplify";
import useSWR from "swr";
import { useRecoilValue } from "recoil";
import { useSnackbar } from "notistack";
import _get from "lodash.get";
import { SingleSelectComponent } from "components/Form";
import { getComparator, stableSort } from "utils/sort";
import debounce from "utils/debounce";
import config from "config";
import { userAtom } from "state";

import StyledAvatar from "components/StyledAvatar";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import useStyle from "./styles";
import TeamNameEdit from "./TeamNameEdit";
import { PeopleItem, PeopleItems } from "./types";
import CircularProgress from "@material-ui/core/CircularProgress";
import { debounce as debounceLodash } from "lodash";
import InfiniteScroll from "react-infinite-scroll-component";
import _, { uniqWith } from "lodash";

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

let usersAPIPayload = {
  path: "",
  method: "",
};

const TeamsPeople = (props: any) => {
  // props.showLayoutLoader();

  const classes = useStyle();
  const { clientId } = useRecoilValue(userAtom);
  const { enqueueSnackbar } = useSnackbar();

  const [order, setOrder] = React.useState("asc");
  const [orderBy, setOrderBy] = React.useState("calories");
  const [peopleData, setPeopleData] = React.useState<any[]>([]);
  const [count, setCount] = React.useState<number>(0);
  const [usersData, setUsersData] = React.useState<any[]>([]);
  const [people, setPeople] = React.useState<any[]>([]);
  const [loading, setLoading] = React.useState(false);

  const myRef = useRef(null);

  const fetchUsers = async (userId?: string) => {
    if (userId) {
      usersAPIPayload.path = `/entity/get-users?lastKey=${userId}`;
    } else {
      usersAPIPayload.path = `/entity/get-users`;
    }
    try {
      const dataUser: [] = await API.get(
        config.apiGateway.NAME,
        usersAPIPayload.path,
        {}
      );
      setUsersData((prev) => uniqWith(prev.concat(dataUser), _.isEqual));
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };

  const fetchPeople = async (userId?: string) => {
    if (userId) {
      peopleAPIPayload.path = `/entity/list-all-members?lastKey=${userId}`;
    } else {
      peopleAPIPayload.path = `/entity/list-all-members`;
    }
    try {
      const result: any = await API.get(
        config.apiGateway.NAME,
        peopleAPIPayload.path,
        {}
      );
      const dataPeople = result ? result.data : [];
      const cnt = result ? result.count : 0;
      setPeopleData((prev) => prev.concat(dataPeople));
      setCount(cnt);
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };

  peopleAPIPayload.path = `/entity/list-all-roles`;
  peopleAPIPayload.method = "GET";

  const { data: rolesData } = useSWR(
    [`${peopleAPIPayload.path}`, peopleAPIPayload],
    {
      suspense: false,
    }
  );

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchUsers(), fetchPeople()])
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  const hasMore = (people.length && people.length % 100 === 0) as boolean;
  const lastMemner = peopleData[peopleData.length - 1];
  const lastKey = lastMemner && lastMemner.user_id;

  const lastUser = usersData[usersData.length - 1];
  const lastUserKey = lastUser && lastUser.entityId;

  const loadMore = async () => {
    setLoading(true);
    await Promise.all([fetchUsers(lastUserKey), fetchPeople(lastKey)]);
    setLoading(false);
  };

  const handleRequestSort = (event: MouseEvent, property: any) => {
    const isAsc = orderBy === property && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(property);
  };

  const handleOnChange = (value: string, userId: string) => {
    debounce(
      API.post(config.apiGateway.NAME, "/entity/update-user-role/", {
        body: {
          role_id: value,
          user_id: userId,
        },
      })
        .then(() => {
          enqueueSnackbar("Role set successfully", SUCCESS_TOAST_OPTIONS);
        })
        .catch(() => {
          enqueueSnackbar("Failed to update role", ERROR_TOAST_OPTIONS);
        }),
      2000
    );
  };
  useEffect(() => {
    // wait for both users and members data
    if (usersData && peopleData && peopleData.length >= usersData.length) {
      const people = peopleData
        ? usersData.map((person: any) => {
            return {
              user: person,
              ...peopleData?.find(
                (user: any) => user.user_id === person.entityId
              ),
            };
          })
        : [];
      setPeople(people);
    }
  }, [peopleData, usersData]);

  const rolesList = rolesData ? rolesData : [];
  let rolesDropDownData: any[] = [];
  for (let i = 0; i < rolesList.length; i++) {
    rolesDropDownData.push({
      label: rolesList[i].roleName,
      value: rolesList[i].entityId,
    });
  }

  let getRoleValue = (roleValue: any) => {
    let item = rolesDropDownData.filter((role: any) => {
      return role.label === roleValue;
    });
    if (item && item.length) return item[0].value;
    else return "";
  };

  const Loading = (
    <div className={classes.loading}>
      <CircularProgress className={classes.CircularProgress} />
    </div>
  );

  return (
    <>
      <InfiniteScroll
        next={loadMore}
        hasMore={hasMore}
        dataLength={people.length}
        loader={Loading}
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
                    user_id: row.user_id,
                    img: `${config.targetBucketUrl}${row.user.attributes.avatarImages?.keys.medium}`,
                    user: row.user ? row.user : [],
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
                          >
                            <StyledAvatar src={agent.img} />
                          </Box>
                          <TeamNameEdit user={agent.user} />
                        </Box>
                      </TableCell>
                      <TableCell width="40%">
                        <Typography
                          className={classes.tableBodySubtitle}
                          variant="subtitle2"
                          gutterBottom
                        >
                          {agent.team}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <SingleSelectComponent
                          id={`role-${row.roleName}-${row.user_id}`}
                          defaultValue={getRoleValue(agent.role)}
                          placeholder="Select a role"
                          options={rolesDropDownData}
                          handleOnChange={(e: any) =>
                            handleOnChange(e, row.user_id)
                          }
                        />
                      </TableCell>
                    </TableRow>
                  );
                }
              )}
            </TableBody>
          </Table>
        </TableContainer>
        {loading && !lastKey && Loading}
        <Divider classes={{ root: classes.tableDivider }} />
      </InfiniteScroll>
    </>
  );
};

export default memo(TeamsPeople);
