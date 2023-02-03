import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    //
  },
  outlinedPrimary: {
    height: 40,
    background: "#2FB0D9",
    border: "3px solid #fff",
    boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
    borderRadius: 90,
    fontStyle: "normal",
    fontWeight: 600,
    fontSize: 13,
    color: "#fff",
    textTransform: "none",
    "&:hover": {
      background: "#5AD787",
      border: "3px solid #fff",
      boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
      borderRadius: 90,
    },
  },
}));

export default Style;
