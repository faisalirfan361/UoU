import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  root: {
    padding: `${theme.spacing(1) / 4}px ${theme.spacing(4)}px`,
    background: theme.footer.background,
    position: "relative",
  },
  listItem: {
    display: "inline-block",
    width: "auto",
    paddingLeft: `${theme.spacing(2)}px`,
    paddingRight: `${theme.spacing(2)}px`,
  },
  listItemText: {
    "& span": {
      color: theme.footer.color,
    },
  },
}));
