import React, { FC, useCallback, useState } from "react";
import {
  Grid,
  Hidden,
  AppBar,
  IconButton,
  Toolbar,
  Typography,
  Badge,
} from "@material-ui/core";
import { Menu as MenuIcon } from "@material-ui/icons";
import { appAtom } from "state";
import { useRecoilState, useRecoilValue } from "recoil";
import { FaRegBell } from "react-icons/fa";
import { userAtom } from "state";

import { AvatarComponent } from "components";
import AvatarHeaderMenu from "components/AvatarHeaderMenu";

import useDashboardHeaderStyles from "./styles";
import { useNotifications } from "context/NotificationsContext";
import { API } from "aws-amplify";
import config from "../../../config";

type AppBarProps = {
  theme?: {};
};

const DashboardHeader: FC<AppBarProps> = ({ children }) => {
  const classes = useDashboardHeaderStyles();
  const { count: notificationsCount, markAllNotificationAsRead } =
    useNotifications();
  const [appAtomState, setAppAtomState] = useRecoilState(appAtom);
  const [menuRootEl, setMenuRootEl] = React.useState(null);
  const { userId } = useRecoilValue(userAtom);

  const handleRootMenuClick = (event: any) => {
    setMenuRootEl(event.currentTarget);
  };

  const handleRootMenuClose = () => {
    setMenuRootEl(null);
  };

  const toggleSidebar = useCallback(() => {
    setAppAtomState({
      ...appAtomState,
      sidebarOpen: !appAtomState.sidebarOpen,
    });
  }, [appAtomState, setAppAtomState]);

  const toggleNotificationsDrawer = useCallback(() => {
    setAppAtomState((state) => ({
      ...state,
      showNotificationsDrawer: !state.showNotificationsDrawer,
    }));
    API.post(config.apiGateway.NAME, `/notification/read-notifications`, {
      body: {
        userId: userId,
      },
    }).then(() => markAllNotificationAsRead("notifications"));
  }, [setAppAtomState]);

  return (
    <>
      <AppBar
        classes={{ root: classes.appBar }}
        position="sticky"
        elevation={0}
      >
        <Toolbar>
          <Grid container alignItems="center" spacing={2}>
            <Hidden mdUp>
              <Grid item>
                <IconButton
                  color="inherit"
                  aria-label="Open drawer"
                  onClick={toggleSidebar}
                >
                  <MenuIcon />
                </IconButton>
              </Grid>
            </Hidden>
            <Grid item>
              <Typography variant="h3">{children}</Typography>
            </Grid>
            <Grid item xs />
            <Grid item>
              <IconButton onClick={toggleNotificationsDrawer}>
                <Badge badgeContent={notificationsCount} color="error">
                  <FaRegBell />
                </Badge>
              </IconButton>
            </Grid>
            <Grid item>
              <IconButton
                aria-controls="simple-menu"
                aria-haspopup="true"
                size="small"
                disableRipple
                onClick={handleRootMenuClick}
              >
                <AvatarComponent dimension={46} />
              </IconButton>
              <AvatarHeaderMenu
                elementId={"simple-menu"}
                element={menuRootEl}
                handleOnClose={handleRootMenuClose}
              />
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>
    </>
  );
};

export default DashboardHeader;
