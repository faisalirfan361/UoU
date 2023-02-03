import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    marginTop: theme.spacing(2),
  },
  avatar: {
    width: 54,
    height: 54,
    padding: 2,
    borderRadius: "50%",
    background: "#252525",
    border: "1px solid #252525",
    backgroundClip: "content-box",
    marginTop: -6,
  },
  subtitle1: {
    // fontFamily: 'Poppins',
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 16,
    color: "#171717",
    textAlign: "left",
  },
  subtitle2: {
    // fontFamily: 'Poppins',
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 14,
    color: "#0065F2",
    textAlign: "right",
  },
}));

export default Style;
