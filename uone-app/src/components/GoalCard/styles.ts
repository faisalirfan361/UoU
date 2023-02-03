import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  newGoalCard: {
    marginTop: 20,
  },
  card: {
    background: "white",
    border: `3px solid ${theme.palette.common.white}`,
    boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
    borderRadius: 5,
    marginTop: 20,
    padding: `${theme.spacing(1.25)}px ${theme.spacing(2)}px`,
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
  midInputContainer: {
    display: "flex",
    justifyContent: "space-between",
  },
  flipWrapper: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end",
  },
  topButtonContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end",
  },
  midGoalContainer: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-end",
  },
  minGoalNumber: {
    textAlign: "start",
  },
  maxGoalNumber: {
    textAlign: "end",
  },
  multiSliderContainer: {
    "& svg": {
      height: 40,
    },
  },
  rangeSlider: {},
  cardTop: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: theme.spacing(4),
  },
  cardTopLabel: {
    fontWeight: 500,
    margin: 0,
    cursor: "pointer",
    width: "max-content",
  },
  cardTopBtn: {
    marginRight: theme.spacing(2)
  },
  labelTxt: {
    color: theme.common.grey[600],
    fontSize: 12,
    textTransform: "uppercase",
    margin: 0,
  },
  penCircle: {
    width: 30,
    height: 30,
    border: `2px solid ${theme.common.uoneLightBlue[400]}`,
    textAlign: "center",
    cursor: "pointer",
    borderRadius: 15,
  },
  pen: {
    color: `${theme.common.uoneLightBlue[400]}`,
    height: "-webkit-fill-available",
  },
  deptSelect: {
    all: "unset",
    color: `${theme.common.uoneLightBlue[400]}`,
    fontWeight: 600,
    marginLeft: "10px",
    "&::after": {
      content: "some content",
      display: "block",
      height: 60,
      marginTop: -60,
    },
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
  kpiInput: {
    display: "block",
    fontSize: 14,
    width: "60%",
    color: theme.common.grey[600],
  },
  topLeftContainer: {
    display: "flex",
    justifyContent: "space-between",
  },
  metricWraper: {
    position: "relative",
    alignSelf: "center",
    textAlign: "center",
  },
  labelTxtValue: {
    color: theme.common.uoneLightBlue[400],
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
}));
