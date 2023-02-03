import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  card: {
    background: "white",
    border: `3px solid ${theme.palette.common.white}`,
    boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
    borderRadius: 5,
    marginTop: 20,
    padding: `${theme.spacing(1.25)}px ${theme.spacing(2)}px`,
  },

  cardTopLabel: {
    fontWeight: 500,
    margin: 0,
    cursor: "pointer",
    width: "max-content",
  },
  labelTxt: {
    color: theme.common.grey[600],
    fontSize: 12,
    textTransform: "uppercase",
    margin: 0,
  },

  outlinedPrimary: {
    minWidth: 110,
    height: 40,
    background: theme.palette.common.white,
    border: `2px solid ${theme.common.uoneLightBlue[400]}`,
    color: `${theme.common.uoneLightBlue[400]}`,
    fontWeight: 600,
    boxSizing: "border-box",
    borderRadius: 90,
    textTransform: "none",
    marginLeft: theme.spacing(4),
    "&:hover": {
      border: `2px solid ${theme.common.uoneLightBlue[500]}`,
    },
  },
  cancelButton: {
    color: theme.common.grey[600],
    fontWeight: 600,
    cursor: "pointer",
  },

  tooltip: {
    background: theme.palette.common.white,
    border: "none",
    borderRadius: 3,
    boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
    color: theme.common.grey[700],
  },
  popper: {
    marginTop: -theme.spacing(5),
  },
  rowSpace: {
    marginBottom: theme.spacing(5),
  },
  editButtonSection: {
    display: "inline",
    float: "right",
    paddingTop: theme.spacing(2),
  },
  goalInput: {
    maxWidth: 150,
    margin: "auto",
  },
  flipBox: {
    alignItems: "center",
  },
  flipTxt: {
    color: theme.common.uoneLightBlue[400],
    marginRight: 10,
  },
  flipIcon: {
    color: theme.common.uoneLightBlue[400],
    cursor: "pointer",
  },
  flipWrapper: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end",
  },
  maxMinInput: {
    "&:disabled": {
      color: theme.palette.common.white,
    },
  },
  buttonsSection: {
    paddingTop: theme.spacing(2),
  },
}));
