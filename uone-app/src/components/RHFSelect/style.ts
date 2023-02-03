import { makeStyles } from "@material-ui/core/styles";

import { zIndex } from "../../constants";

const Style = makeStyles((theme) => ({
  root: {
    //,
  },
  select: {
    zIndex: zIndex.select,
  },
  selectPapper: {
    boxShadow: "0px 2px 10px rgb(0 0 0 / 19%)"
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

export default Style;
