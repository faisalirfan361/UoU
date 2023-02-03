import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  outlinedPrimary: {
    height: 40,
    background: "#D9392F",
    border: `3px solid ${theme.palette.common.white}`,
    boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
    borderRadius: 90,
    fontStyle: "normal",
    fontWeight: 600,
    fontSize: 13,
    color: theme.palette.common.white,
    textTransform: "none",
    "&:hover": {
      background: "#5AD787",
    border: `2px solid ${theme.palette.common.white}`,
    },
  },
}));

export default Style;
