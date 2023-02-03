import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  outlinedPrimary: {
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
    height: 40,
    background: `${theme.common.uoneLightBlue[500]}`,
    border: `3px solid ${theme.palette.common.white}`,
    boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
    borderRadius: 90,
    fontStyle: "normal",
    fontWeight: 600,
    fontSize: 13,
    color:`${theme.palette.common.white}`,
    textTransform: "none",
    "&:hover": {
      background: `${theme.common.uoneNeonGreen[500]}`,
      border: `3px solid ${theme.palette.common.white}`,
      boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
      borderRadius: 90,
    },
  },
}));