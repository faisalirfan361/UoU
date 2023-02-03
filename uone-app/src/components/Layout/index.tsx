import React, { useState } from "react";
import Grid from "@material-ui/core/Grid";
import { FaRegUser } from "react-icons/fa";
import NotificationsIcon from "@material-ui/icons/Notifications";
import { useRecoilValue } from "recoil";
import { NavLink } from "react-router-dom";
import slugify from "react-slugify";
import _intersectionBy from "lodash.intersectionby";
import Modal from "react-modal";
import {
  Link,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from "@material-ui/core";
import { userAtom } from "state";
import { AvatarComponent, ImageUpload } from "components";
import config from "config";
import Children from "react-children-utilities";

import {
  adminMenuItems,
  agentMenuItems,
  managerMenuItems,
  MenuVariant,
  teamMenuItems,
} from "../../constants";

import Styles from "./style";
import IProps from "./types";

import logo from "../../assets/img/logo.png";
import LayoutLoading from "./Loading";

const Layout: React.FC<IProps> = ({ title, menuVariant, children }) => {
  const [layoutLoader, setLayoutLoader] = React.useState(false);

  const showLayoutLoader = () => {
    setLayoutLoader(true);
  };

  const hideLayoutLoader = () => {
    setLayoutLoader(false);
  };

  const childrenWithProps = Children.deepMap(children, function (child: any) {
    if (React.isValidElement(child)) {
      const extraChildProps = {
        showLayoutLoader: showLayoutLoader,
        hideLayoutLoader: hideLayoutLoader,
      };
      return React.cloneElement(child, extraChildProps);
    } else {
      return child;
    }
  });

  const styles = Styles();
  const { clientId } = useRecoilValue(userAtom);

  let menuItems = [];

  let [modalIsOpen, setModalIsOpen] = useState(false);
  let [uploadMsg, setUploadMsg] = useState("");

  const setModalIsOpenToTrue = () => {
    setModalIsOpen(true);
  };

  const setModalIsOpenToFalse = () => {
    setModalIsOpen(false);
  };

  const setModalIsUploadedToTrue = (setTo: any) => {
    if (!setTo) return "PROFILE";
    setUploadMsg(setTo);
  };

  const { username, roleName, modules, avatarImages } =
    useRecoilValue(userAtom);

  let friendlyUsername = `${slugify(username.toLowerCase())}`;
  let routeOption = "";

  // TODO both base url and client id should not be coming from api
  let avatarSrc = config.targetBucketUrl + `${avatarImages?.keys.medium}`;

  switch (roleName) {
    case "Admin":
      routeOption = `/dashboard/admin/${friendlyUsername}`;
      break;
    case "Manager":
      routeOption = `/dashboard/manager/${friendlyUsername}`;
      break;
    case "Agent":
      routeOption = `/dashboard/agent/${friendlyUsername}`;
      break;
    case "Team lead":
      routeOption = `/dashboard/teamlead/${friendlyUsername}`;
      break;
    default:
      routeOption = "/dashboard/error";
  }

  if (menuVariant === MenuVariant.ADMIN) {
    menuItems = adminMenuItems;
  } else if (menuVariant === MenuVariant.AGENT) {
    menuItems = agentMenuItems;
  } else if (menuVariant === MenuVariant.MANAGER) {
    menuItems = managerMenuItems;
  } else if (menuVariant === MenuVariant.TEAM_LEAD) {
    menuItems = teamMenuItems;
  } else {
    menuItems = agentMenuItems;
  }

  const slugs = modules
    .filter((module: any) => module.canView)
    .map((module: any) => ({
      path: module.moduleSlug,
    }));

  if (process.env.NODE_ENV === "development") {
    console.log("\n---===--- Layout - menuItems ---===---\n", menuItems);
    console.log("\n---===--- Layout - menuItems[0] ---===---\n", menuItems[0]);
    console.log("\n---===--- Layout - slugs ---===---\n", slugs);
  }

  const menuItemsFiltered = _intersectionBy(menuItems, slugs, "path");

  if (process.env.NODE_ENV === "development") {
    console.log(
      "\n---===--- Layout - menuItemsFiltered ---===---\n",
      menuItemsFiltered
    );
  }

  menuItemsFiltered.unshift(menuItems[0]);

  menuItems = menuItemsFiltered;

  return (
    <div className={styles.root}>
      <LayoutLoading isInProgress={layoutLoader} />
      <Grid container className={styles.topSection}>
        <Grid
          item
          xs={6}
          sm={6}
          md={2}
          lg={2}
          xl={2}
          className={styles.logoContainer}
        >
          <img src={logo} className={styles.logoImg} alt="HeyDay Now" />
        </Grid>
        <Grid
          item
          xs={6}
          sm={6}
          md={10}
          lg={10}
          xl={10}
          className={styles.navContainer}
        >
          <Grid container>
            <Grid item xs={6} sm={6} md={6} lg={6} xl={6}>
              <h1>{title}</h1>
            </Grid>
            <Grid
              item
              xs={6}
              sm={6}
              md={6}
              lg={6}
              xl={6}
              className={styles.navRightSection}
            >
              <Grid container>
                <Grid item xs={8} sm={8} md={8} lg={8} xl={8}>
                  {/* <Paper
                    component="form"
                    className={styles.searchInput}
                    elevation={0}
                  >
                    <IconButton type="submit" aria-label="search">
                      <SearchIcon style={{ fill: "grey" }} />
                    </IconButton>
                    <InputBase
                      placeholder="Search..."
                      inputProps={{ "aria-label": "Search" }}
                      className={styles.inputBase}
                    />
                  </Paper> */}
                </Grid>
                <Grid
                  item
                  xs={2}
                  sm={2}
                  md={2}
                  lg={2}
                  xl={2}
                  className={styles.notificationIconContainer}
                >
                  <NotificationsIcon
                    fontSize="large"
                    style={{ fill: "grey" }}
                  />
                </Grid>
                <Grid
                  item
                  xs={2}
                  sm={2}
                  md={2}
                  lg={2}
                  xl={2}
                  className={styles.notificationIconContainer}
                >
                  <Modal
                    className={styles.modalcontent}
                    isOpen={modalIsOpen}
                    ariaHideApp={false}
                    style={{ overlay: { zIndex: 9999 } }}
                  >
                    <button
                      onClick={setModalIsOpenToFalse}
                      className={styles.modalClose}
                    >
                      x
                    </button>
                    <ImageUpload
                      callback={() => {}}
                      type="PROFILE"
                    ></ImageUpload>
                    {uploadMsg && (
                      <div className={styles.msgImageUpload}>{uploadMsg}</div>
                    )}
                  </Modal>
                  <Grid onClick={setModalIsOpenToTrue}>
                    <AvatarComponent src={avatarSrc} />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
      <Grid container>
        <Grid
          item
          xs={6}
          sm={6}
          md={2}
          lg={2}
          xl={2}
          className={styles.menuContainer}
        >
          <List>
            {menuItems.map((menuItem, index) => (
              <ListItem
                key={index}
                exact
                component={NavLink}
                to={`${routeOption}${menuItem.path}`}
                className={styles.menuItem}
                activeClassName={styles.menuItemActive}
              >
                <ListItemIcon className={styles.menuIcon}>
                  <menuItem.iconClass />
                </ListItemIcon>
                <ListItemText
                  className={styles.menuLabel}
                  primary={menuItem.title}
                />
              </ListItem>
            ))}
          </List>
          <Grid
            container
            className={`${styles.menuItem} ${styles.signOutItem}`}
          >
            <Link href={`/logout`} underline="none" color="inherit">
              <span className={styles.menuIcon}>
                <FaRegUser />
              </span>
              <span>Sign Out</span>
            </Link>
          </Grid>
        </Grid>
        <Grid
          item
          xs={6}
          sm={6}
          md={10}
          lg={10}
          xl={10}
          className={styles.contentContainer}
          id={"content"}
        >
          {childrenWithProps}
        </Grid>
      </Grid>
    </div>
  );
};

export default Layout;
