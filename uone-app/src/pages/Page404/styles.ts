import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  root: {
    display: "flex",
    height: "100vh",
    justifyContent: "center",
    alignItems: "center",
    padding: `${theme.spacing(6)}px`,
    textAlign: "center",
    background: "transparent",

    [theme.breakpoints.up("md")]: {
      padding: `${theme.spacing(10)}px`,
    },
  },
}));
