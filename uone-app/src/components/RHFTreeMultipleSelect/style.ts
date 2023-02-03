import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  selectPapper: {
    boxShadow: "0px 2px 10px rgb(0 0 0 / 19%)",
  },
  infiniteScroll: {
    overflow: "unset !important",
  },
  loading: {
    width: "100%",
    display: "flex",
    justifyContent: "center",
    padding: theme.spacing(1),
  },
  CircularProgress: {
    color: theme.common.uoneLightBlue[500],
  },
}));
