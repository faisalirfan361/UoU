import "react-perfect-scrollbar/dist/css/styles.css";
import React, { useEffect } from "react";
import { NavLink } from "react-router-dom";
import PerfectScrollbar from "react-perfect-scrollbar";
import {
  Drawer,
  Typography,
  Hidden,
  useMediaQuery,
  useTheme,
  ListItemIcon,
  MenuList,
  MenuItem,
} from "@material-ui/core";
import { useAbility } from "@casl/react";

import { appAtom, userAtom } from "state";
import { useRecoilState, useRecoilValue } from "recoil";

import Brand from "./Brand";
import useSidebarClasses from "./styles";
import protectedRoutes from "../../../routes/protected";
import { AbilityContext, Can } from "context/Ability/Can";

type SidebarPropsType = {
  variant?: "permanent" | "persistent" | "temporary";
  onClose?: () => void;
};

const DashboardSidebar: React.FC<SidebarPropsType> = () => {
  const classes = useSidebarClasses();
  const ability = useAbility(AbilityContext);
  const [appAtomState, setAppAtomState] = useRecoilState(appAtom);
  const { roleName } = useRecoilValue(userAtom);

  const theme = useTheme();
  const isDesktopView = useMediaQuery(theme.breakpoints.up("md"));

  useEffect(() => {
    if (isDesktopView && appAtomState.sidebarOpen) {
      setAppAtomState({ ...appAtomState, sidebarOpen: false });
    }
  }, [isDesktopView, appAtomState, setAppAtomState]);

  const drawerContent = (
    <>
      <Brand />
      <PerfectScrollbar>
        <MenuList disablePadding>
          {protectedRoutes.map((route) => {
            return (
              <Can I="view" a={route.slug} key={route.slug}>
                <NavLink
                  to={route.path}
                  className={classes.menuItem}
                  activeClassName={classes.activeMenu}
                >
                  <MenuItem className={classes.sidebarMenu}>
                    <ListItemIcon className={classes.sidebarMenuIcon}>
                      {route.icon}
                    </ListItemIcon>
                    <Typography variant="inherit" noWrap>
                      {route.label}
                    </Typography>
                  </MenuItem>
                </NavLink>
              </Can>
            );
          })}
        </MenuList>
      </PerfectScrollbar>
    </>
  );

  return (
    <>
      <Hidden mdUp implementation="css">
        <Drawer
          variant="temporary"
          classes={{ root: classes.drawer, paper: classes.paper }}
          open={appAtomState.sidebarOpen}
          onClose={() => {
            setAppAtomState({
              ...appAtomState,
              sidebarOpen: !appAtomState.sidebarOpen,
            });
          }}
        >
          {drawerContent}
        </Drawer>
      </Hidden>
      <Hidden smDown implementation="css">
        <Drawer
          variant="permanent"
          classes={{ root: classes.drawer, paper: classes.paper }}
          open
        >
          {drawerContent}
        </Drawer>
      </Hidden>
    </>
  );
};

export default DashboardSidebar;
