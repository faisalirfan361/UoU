import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    background: "transparent",
    borderRadius: "1.5vw",
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
  },
  card: {
    background: "transparent",
    borderRadius: "1.5vw",
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
  },
  cardContent: {
    "&:last-child": {
      paddingBottom: theme.spacing(1) + 2,
    },
  },
}));

export default Style;
