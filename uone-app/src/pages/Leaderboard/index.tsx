import React, { useEffect } from "react";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import Grid from "@material-ui/core/Grid";
import { Box } from "@material-ui/core";

import PostsList from "../../containers/Shared/PostsList";
import LeaderboardWithFilter from "../../containers/Shared/Leaderboard/LeaderboardWithFilter";
import useStyle from "./styles";
import { useLayoutContext } from "../../layouts/LayoutProvider";
import { Link } from "react-router-dom";
import { useQuery } from "hooks/useQueryParams";

enum TabIndex {
  LEADERBOARD = 0,
  FEED = 1,
}

const LeaderboardPage = () => {
  const { setLayoutTitle } = useLayoutContext();
  const queryParams = useQuery();
  const urlTabIndexParam = queryParams.get("tabIndex");
  const [tabIndex, setTabIndex] = React.useState(TabIndex.LEADERBOARD);

  useEffect(() => {
    if (urlTabIndexParam) {
      setTabIndex(+urlTabIndexParam);
    }
  }, [urlTabIndexParam]);

  const classes = useStyle();

  useEffect(() => {
    setLayoutTitle("Leaderboard");
  }, [setLayoutTitle]);

  return (
    <>
      <Grid container direction="row" className={classes.tabsContainer}>
        <Tabs
          value={tabIndex}
          indicatorColor="primary"
          textColor="primary"
          onChange={(_, newValue) => setTabIndex(newValue)}
          className={classes.tabs}
        >
          <Tab label="Leaderboard" to="/leaderboard" component={Link} />
          <Tab label="Feed" to="/leaderboard?tabIndex=1" component={Link} />
        </Tabs>
      </Grid>

      <Box className={classes.tabContent}>
        {tabIndex === TabIndex.LEADERBOARD && <LeaderboardWithFilter />}
        {tabIndex === TabIndex.FEED && <PostsList />}
      </Box>
    </>
  );
};

export default LeaderboardPage;
