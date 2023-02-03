import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  outlinedPrimary: {
    minWidth: 125,
    height: 40,
    background: theme.palette.common.white,
    border: `1px solid ${theme.common.uoneLightBlue[400]}`,
    color: `${theme.common.uoneLightBlue[400]}`,
    boxSizing: "border-box",
    borderRadius: 90,
    textTransform: "none",
    "&:hover": {
      border: `1px solid ${theme.common.uoneLightBlue[600]}`,
    },
  },
}));
