import {
  MenuList,
  MenuItem,
  ListItemIcon,
  Typography,
} from "@material-ui/core";
import clsx from "clsx";
import { FaRegLifeRing, FaSignOutAlt } from "react-icons/fa";
import { NavLink } from "react-router-dom";

import useSidebarClasses from "./styles";

export default function SidebarFooter() {
  const classes = useSidebarClasses();
  return (
    <MenuList disablePadding>
      <MenuItem
        className={clsx(
          classes.menuItem,
          classes.sidebarMenu,
          classes.helpButton
        )}
      >
        <ListItemIcon className={classes.sidebarMenuIcon}>
          <FaRegLifeRing />
        </ListItemIcon>
        <Typography variant="inherit" noWrap>
          Help
        </Typography>
      </MenuItem>
      <NavLink
        to="/logout"
        className={classes.menuItem}
        activeClassName={classes.activeMenu}
      >
        <MenuItem className={classes.sidebarMenu}>
          <ListItemIcon className={classes.sidebarMenuIcon}>
            <FaSignOutAlt />
          </ListItemIcon>
          <Typography variant="inherit" noWrap>
            Signout
          </Typography>
        </MenuItem>
      </NavLink>
    </MenuList>
  );
}
