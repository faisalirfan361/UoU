import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    padding: theme.spacing(1),
    paddingRight: theme.spacing(2),
  },
  thumbnail: {
    width: theme.spacing(7),
    height: theme.spacing(7),
  },
  challengeDetails: {
    marginLeft: theme.spacing(3),
    textAlign: "left",
  },
  title: {
    color: "#fdb515",
    fontSize: "1.2rem",
    fontWeight: "normal",
    textTransform: "uppercase",
    marginBottom: theme.spacing(1),
  },
  metric: {
    color: "#ffffff",
    fontSize: "1rem",
    marginBottom: theme.spacing(1),
  },
  description: {
    color: "#ffffff",
    fontSize: ".95rem",
    marginBottom: theme.spacing(1),
  },
  details: {
    color: "#cccccc",
    fontSize: ".80rem",
    fontWeight: "normal",
  },
  agent: {
    width: theme.spacing(1),
    height: theme.spacing(1),
  }
}));

export default Style;
