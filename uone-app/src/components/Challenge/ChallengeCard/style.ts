import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  root: {
    padding: `${theme.spacing(3)}px`,
    backgroundColor: theme.palette.common.white,
    margin: "0px auto 20px auto",
    boxShadow: "0px 1px 3px rgba(0, 0, 0, 0.25)",
  },
}));
