import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  root: {
    display: "flex",
    minHeight: "100vh",
  },
  appContent: {
    display: "flex",
    flex: 1,
    flexDirection: "column",
    maxWidth: "100%",
  },
  mainContent: {
    flex: 1,
    background: theme.palette.background.default,
    boxShadow: "none",

    padding: "1em",
    [theme.breakpoints.up("sm")]: {
      padding: "2em",
    },
  },
}));
