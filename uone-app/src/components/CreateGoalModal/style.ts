import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  popupContainer: {
    minHeight: 400,
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
    paddingBottom: theme.spacing(3),
  },
  btnCancel: {
    marginRight: theme.spacing(2),
  }
}));
