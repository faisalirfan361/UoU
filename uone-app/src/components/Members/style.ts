import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    display: "flex",
  },
  member: {
    padding: 0,
    margin: 0,
    marginRight: theme.spacing(3) - 4,
    marginBottom: theme.spacing(3) - 4,
    maxWidth: 180,
  },
}));

export default Style;
