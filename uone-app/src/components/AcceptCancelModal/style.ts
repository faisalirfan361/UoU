import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  container: {
    width: 300,
  },
  cancelButton: {
    color: theme.common.uoneShineViolet[500],
    border: `3px solid ${theme.common.uoneShineViolet[500]}`,
    "&:hover": {
      backgroundColor: theme.common.uoneShineViolet[500],
      border: `3px solid ${theme.common.uoneShineViolet[500]}`,
      color: theme.palette.common.white,
    },
  },
}));
