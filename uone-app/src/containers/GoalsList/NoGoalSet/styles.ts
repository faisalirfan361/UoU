import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  headingText: {
    marginTop: theme.spacing(2),
    textAlign: "center",
    color: theme.common.grey[900],
    fontSize: 24,
  },
  text: {
    marginTop: theme.spacing(0.5),
    textAlign: "center",
    color: theme.common.grey[700],
    fontSize: 16,
  },
  goalTextBtn: {
    marginTop: theme.spacing(5),
    textAlign: "center",
    fontSize: 16,
    fontWeight: 600,
    color: theme.common.uoneLightBlue[600],
    cursor: "pointer",
  },
  
}));

export default Style;
