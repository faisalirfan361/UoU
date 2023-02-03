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
    marginRight: 10
  },
  flipIcon: {
    color: theme.common.uoneLightBlue[400],
    cursor: "pointer"
  },
  midInputContainer: {
    display: "flex",
    justifyContent: "space-between"
  },
  flipWrapper: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end"
  },
  topButtonContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end"
  },
  midGoalContainer: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-end"
  },
  minGoalNumber: {
    textAlign:"start"
  },
  maxGoalNumber: {
    textAlign:'end'
  },
  multiSliderContainer: {
    '& svg': {
      height: 40,
    },
    
  },rangeSlider:{}
}));