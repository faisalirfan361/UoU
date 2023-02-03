import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  icon: {
    width: "40px",
    height: "40px",
  },
  listItemIcon: {
    color: theme.common.uoneLightBlue[500],
    justifyContent: "center",
  },
  listItemText: {
    paddingLeft: theme.spacing(2),
  },
}));
