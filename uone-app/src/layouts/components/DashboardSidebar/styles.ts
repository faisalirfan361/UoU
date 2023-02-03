import { makeStyles } from "@material-ui/core";
import { darken, lighten } from "polished";

const drawerWidth = 258;
export default makeStyles((theme) => ({
  drawer: {
    borderRight: 0,
    [theme.breakpoints.up("sm")]: {
      width: drawerWidth,
      flexShrink: 0,
    },
  },
  paper: {
    width: drawerWidth,
    background: theme.sidebar.background,
  },
  sidebarHeader: {
    display: "flex",
    minHeight: theme.spacing(16),
    background: theme.sidebar.header.background,
    borderBottom: `1px solid ${theme.sidebar.menu.background}`,
    alignItems: "center",
    justifyContent: "center",
  },
  brand: {
    "& img": {
      maxHeight: 100,
      minHeight: 35,
      maxWidth: 208,
    },
  },
  sidebarMenu: {
    flex: 1,
    fontSize: theme.sidebar.menu.fontSize,
    paddingTop: theme.spacing(3),
    paddingBottom: theme.spacing(3),
    borderBottom: `1px solid ${lighten(0.05, theme.sidebar.menu.background)}`,
  },
  sidebarMenuIcon: {
    fontSize: theme.sidebar.menu.iconFontSize,
    minWidth: theme.sidebar.menu.iconMinWidth,
    color: "inherit",
  },
  menuItem: {
    display: "flex",
    textDecoration: "none",
    color: theme.palette.grey[500],
    "&:hover": {
      color: theme.palette.grey[400],
      background: darken(0.05, theme.sidebar.menu.background),
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
  helpButton: {
    borderTop: `1px solid ${lighten(0.05, theme.sidebar.menu.background)}`,
  },
  // necessary for content to be below app bar
  toolbar: theme.mixins.toolbar,
  drawerPaper: {
    width: drawerWidth,
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(3),
  },
}));
