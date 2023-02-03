import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  root: {
    width: theme.spacing(5),
    height: theme.spacing(5),
    border: `2px solid ${theme.common.uoneLightBlue[500]}`,
    padding: theme.spacing(1),
    color: theme.common.uoneLightBlue[500],
    background: theme.palette.common.white,
    "&:hover": {
      background: theme.palette.common.white,
    },
  },
}));
