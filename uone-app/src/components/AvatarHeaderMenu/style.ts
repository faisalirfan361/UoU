import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  sidebarMenu: {
    flex: 1,
    fontSize: theme.sidebar.menu.fontSize,
    paddingTop: theme.spacing(3),
    paddingBottom: theme.spacing(3),
  },
  sidebarMenuIcon: {
    fontSize: theme.sidebar.menu.iconFontSize,
    minWidth: theme.sidebar.menu.iconMinWidth,
    color: "inherit",
  },
  menuItem: {
    display: "flex",
    textDecoration: "none",
    color: theme.palette.common.black,
    "&:hover": {
      background: theme.palette.grey[200],
    },
  },
  activeMenu: {
    background: theme.sidebar.menu.active.background,
    color: theme.sidebar.menu.active.color,
    "&:hover": {
      background: theme.sidebar.menu.active.background,
      color: theme.sidebar.menu.active.color,
    },
  },
}));
