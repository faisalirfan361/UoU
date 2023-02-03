import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root:{
    paddingTop: theme.spacing(5),
    paddingBottom: theme.spacing(3),
    height: 320,
    overflowY: "auto",
    overflowX: "hidden",
    marginBottom: 20
  },
  progressBar: {
    position: "relative",
  },
  label: {
    display: "inline-block",
    position: "absolute",
    "&:not": {
      "&:first-child": {
        borderLeft: "2px solid transparent",
      },
    },
  },
  progressBarProgressBg: {
    position: "relative",
    height: 25,
    marginBottom: -10,
    backgroundColor: "rgb(204, 204, 204)",
    borderRadius: 20,
    overflow: "hidden",
  },
  progressBarProgress: {
    position: "absolute",
    top: 0,
    height: "100%",
    width: 0,
    backgroundColor: "rgb(204, 204, 204)",
    transition: "all 0.2s ease-in-out",
  },
  progressBarGoal: {
    position: "absolute",
    top: 0,
    height: 25,
    width: 5,
    backgroundColor: "transparent",
    transition: "all 0.2s ease-in-out",
    borderRight: "1px dotted black",
  },
  progressLabel: {
    color: "#ffffff",
    position: "relative",
    fontWeight: "bold",
    top: -12,
  },
  progressBarSeparator: {
    height: "100%",
    width: 1,
    backgroundColor: "red",
  },
  tooltip: {
    backgroundColor: "#82c341",
    fontSize: ".85rem",
  },
  tooltipArrow: {
    backgroundColor: "#82c341",
  },
}));

export default Style;
