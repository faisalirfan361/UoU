import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  badges: {
    display: "grid",
    gridTemplateColumns: "repeat(7, 50px)",
    gridAutoRows: "50px",
    gridGap: "8px",
    justifyContent: "center",
    marginTop: "1em",
  },
  badgesPagination: {
    position: "relative",
  },
  badgeNumber: {
    position: "absolute",
    left: "2em",
  },
  dotsButton: {
    fontSize: "0.8em",
    padding: 0,
    color: theme.common.grey[300],
  },
  dotsButtonActive: {
    color: theme.common.uoneLightBlue[500],
  },
}));
