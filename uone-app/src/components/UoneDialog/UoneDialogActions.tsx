import {
  Theme,
  withStyles,
  DialogActions as MuiDialogActions,
} from "@material-ui/core";

const DialogActions = withStyles((theme: Theme) => ({
  root: {
    margin: 0,
    padding: theme.spacing(1),
  },
}))(MuiDialogActions);

export default DialogActions;
