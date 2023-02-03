import React, { FC } from "react";
import { Box, CssBaseline, Paper } from "@material-ui/core";

import Sidebar from "./components/DashboardSidebar";
import Header from "./components/DashboardHeader";
import Footer from "./components/DashboardFooter";
import NotificationsDrawer from "components/NotificationsDrawer";

import useStyles from "./styles";
import { useLayoutContext } from "./LayoutProvider";

const Dashboard: FC = ({ children }) => {
  const { title } = useLayoutContext();
  const classes = useStyles();

  /**
   * currently our footers links are not with pages, so
   * we are commenting the footer for now and we need to
   * hook up the footer links in future.
   *
   */
  return (
    <Box className={classes.root}>
      <CssBaseline />
      <Sidebar />
      <Box className={classes.appContent}>
        <Header>{title}</Header>
        <Paper className={classes.mainContent}>{children}</Paper>
        {/* <Footer /> */}
        <NotificationsDrawer />
      </Box>
    </Box>
  );
};

export default Dashboard;
