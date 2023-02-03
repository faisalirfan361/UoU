import React, { FC, useState } from "react";
import {
  Menu,
  MenuList,
  MenuItem,
  ListItemIcon,
  Typography,
} from "@material-ui/core";
import clsx from "clsx";
import { FaRegLifeRing, FaSignOutAlt } from "react-icons/fa";
import { NavLink } from "react-router-dom";

import AvatarHeaderMenuStyle from "./style";
import AvatarHeaderMenuProps from "./types";
import { FaqModal } from "components/FaqModal";

const AvatarHeaderMenu: FC<AvatarHeaderMenuProps> = ({
  elementId,
  element,
  handleOnClose,
}) => {
  const [showFaqModal, setShowFaqModal] = useState(false);
  const classes = AvatarHeaderMenuStyle();

  return (
    <>
      <Menu
        id={elementId}
        anchorEl={element}
        keepMounted={false}
        open={Boolean(element)}
        onClose={handleOnClose}
        getContentAnchorEl={null}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <MenuItem
          className={clsx(classes.menuItem, classes.sidebarMenu)}
          onClick={() => {
            setShowFaqModal(true);
          }}
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
              Sign out
            </Typography>
          </MenuItem>
        </NavLink>
      </Menu>
      <FaqModal
        onClose={() => {
          setShowFaqModal(false);
        }}
        open={showFaqModal}
      />
    </>
  );
};

export default AvatarHeaderMenu;
