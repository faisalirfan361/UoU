import React, { useEffect } from "react";
import { Box } from "@material-ui/core";

import LeaderboardPresentation from "../../../containers/Shared/Leaderboard/LeaderboardPresentation";
import useStyle from "./styles";
import { useLayoutContext } from "../../../layouts/LayoutProvider";

const LeaderboardPage = () => {
  const { setLayoutTitle } = useLayoutContext();
  const classes = useStyle();

  useEffect(() => {
    setLayoutTitle("Leaderboard");
  }, [setLayoutTitle]);

  return (
    <Box className={classes.root}>
      <LeaderboardPresentation />
    </Box>
  );
};

export default LeaderboardPage;
