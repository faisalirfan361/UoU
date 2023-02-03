import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  drawerHeader: {
    display: "flex",
    alignItems: "center",
    padding: theme.spacing(0, 2),
    ...theme.mixins.toolbar,
    justifyContent: "space-between",
  },
  drawerHeaderTitle: {
    display: "flex",
    alignItems: "center",
  },
  drawerHeaderIcon: {
    lineHeight: "1em",
    marginRight: theme.spacing(1),
  },
  drawerCloseButton: {
    color: theme.common.uoneLightBlue[500],
  },
  listItemIcon: {
    color: theme.common.uoneLightBlue[500],
  },
  skeletonWithMargin: {
    marginBottom: theme.spacing(1),
  },
  drawerNotificationsList: {
    padding: theme.spacing(2, 3),
  },
}));
