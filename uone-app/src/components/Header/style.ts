import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    backgroundColor: "#555555",
    color: theme.palette.text.primary,
    borderRadius: "1.5vw",
    overflow: "hidden",
  },
  logo: {
    marginRight: "auto",
    marginLeft: theme.spacing(3),
  },
  customerName: {
    color: "#f8ae48",
  },
  list: {
    minWidth: 200,
    marginTop: theme.spacing(1),
  },
  menuTitle: {
    pointerEvents: "none",
    userSelect: "none",
  },
  menuCategory: {
    userSelect: "none",
    color: theme.palette.text.hint,
    textTransform: "uppercase",
  },
}));

export default Style;
