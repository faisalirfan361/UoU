import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    paddingTop: theme.spacing(2),
    paddingBottom: theme.spacing(2),
    "& .label": {
      margin: 0,
      fontSize: 11,
      fontWeight: 500,
    },
    "& .info": {
      margin: 0,
      fontSize: 14,
      fontWeight: 400,
      textTransform: "uppercase",
    },
  },
}));

export default Style;
