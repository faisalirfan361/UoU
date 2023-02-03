import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  deleteBtn: {
    color: "red",
  },
  heading: {
    color: theme.common?.uoneLightBlue[400],
    fontWeight: 500,
    fontSize: "18px",
    lineHeight: "27px",
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  coinsWrapper: {
    display: "flex",
    alignItems: "center",
  },
  coins: {
    fontWeight: 600,
    fontSize: "16px",
    lineHeight: "24px",
  },
  challengeTextColor: {
    color: theme.palette.common?.white,
  },
  actionMenuBtn: {
    color: theme.common?.grey[600],
  },
}));
