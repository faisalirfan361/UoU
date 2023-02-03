import { makeStyles, darken } from "@material-ui/core/styles";

export enum MeterColor {
  RED = "#EF647B",
  YELLOW = "#FCD248",
  GREEN = "#5AD787",
}

const Styles = makeStyles((theme) => {
  return {
    root: {
      padding: theme.spacing(1),
    },
    paper: {
      padding: theme.spacing(2),
    },
    container: {
      paddingTop: theme.spacing(2),
    },
    performanceIcon: {
      fontSize: "31px",
      position: "relative",
      top: "5px",
    },
    headerContainer: {
      display: "flex",
      flexDirection: "row",
      gap: 10,
      flexBasis: "unset !important",
    },
    graphCont: {
      width: "100%",
      display: "flex",
      alignItems: "center",
    },
    line: {
      maxWidth: "16.666667% !important",
      flexBasis: "16.666667% !important",
    },
    chart: {
      maxWidth: "83.333333% !important",
      flexBasis: "83.333333% !important",
    },
    graphLabel: {
      fontSize: "12px",
    },
    statusContainer: {
      display: "flex",
      flexDirection: "row",
      gap: 10,
      flexBasis: "unset !important",
    },
    meterContainer: {
      display: "flex",
      flexDirection: "row",
      marginTop: theme.spacing(10),
      marginBottom: theme.spacing(8),
      height: theme.spacing(2),
      borderRadius: 5,
      position: "relative",
    },
    redMeter: {
      height: "100%",
      backgroundColor: MeterColor.RED,
      width: "20%",
    },
    yellowMeter: {
      height: "100%",
      backgroundColor: MeterColor.YELLOW,
      width: "60%",
    },
    greenMeter: {
      height: "100%",
      backgroundColor: MeterColor.GREEN,
      width: "20%",
    },
    left: {
      borderTopLeftRadius: 5,
      borderBottomLeftRadius: 5,
    },
    right: {
      borderTopRightRadius: 5,
      borderBottomRightRadius: 5,
    },
    meterLine: {
      height: "200%",
      position: "absolute",
      top: "-50%",
      left: "calc(100% - 2px)",
      width: 2,
      backgroundColor: "black",
    },
    goalLine: {
      height: "200%",
      position: "absolute",
      top: "-50%",
      width: 2,
      backgroundColor: darken(MeterColor.GREEN, 0.2),
    },
    goalLineRight: {
      left: "calc(80% - 2px)",
    },
    goalLineLeft: {
      left: "calc(20% - 2px)",
    },
    positionText: {
      position: "absolute",
      top: "-40px",
      left: 0,
      fontSize: 10,
    },
    goalText: {
      position: "absolute",
      top: "30px",

      fontSize: 10,
      fontWeight: 600,
      color: MeterColor.GREEN,
      borderRadius: "50%",
      textAlign: "center",
      border: "3px solid",
      padding: 5,
    },
    goalTextRight: {
      right: "20%",
      transform: "translate(50%,0)",
    },
    goalTextLeft: {
      left: "20%",
      transform: "translate(-50%,0)",
    },
    tooltip: {
      background: theme.palette.common.white,
      border: "none",
      borderRadius: 3,
      boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
      color: theme.common.grey[700],
    },
    popper: {
      marginTop: -theme.spacing(4),
    },
    labelTypography: {
      cursor: "pointer",
    },
    confetti: {
      display: "none",
    },
    durationContainer: {
      marginTop: theme.spacing(2),
      paddingTop: theme.spacing(1),
      borderTop: `1px solid ${theme.common.grey[500]}`,
    },
    durationHeaderText: {
      fontSize: 11,
      color: theme.common.grey[700],
    },
    durationFlexBasic: {
      flexBasis: "16.666667% !important",
    },
    durationBodyText: {
      fontSize: 14,
      color: theme.common.grey[900],
      textTransform: "capitalize",
    },
    durationDivider: {
      height: 30,
    },
  };
});

export const AvatarStyles = (props: { linePosition?: string }) =>
  makeStyles((theme) => ({
    root: {
      position: "absolute",
      top: "-55px",
      left: props.linePosition,
      transform: "translate(-50%, 0)",
      height: 50,
      width: 50,
      boxShadow: "none",
    },
  }));

export const getGoalIconStyles = (linePosition: string) => ({
  position: "absolute",
  top: "25px",
  left: "calc(80% - 2px)",
  transform: "translate(-50%, 0)",
  height: 45,
  width: 45,
  background: "white",
  boxShadow: `0 0 0 3px ${MeterColor.GREEN}`,
});

export default Styles;
