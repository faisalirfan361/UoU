import { rgba, darken } from "polished";
import uoneLightBlue from "./colors/lightBlue";
import uoneLightGreen from "./colors/lightGreen";

const overrides = {
  MuiBackdrop: {
    root: {
      backgroundColor: rgba(0, 0, 0, 0.7),
    },
  },
  MuiCard: {
    root: {
      borderRadius: "6px",
      boxShadow:
        "rgba(50, 50, 93, 0.025) 0px 2px 5px -1px, rgba(0, 0, 0, 0.05) 0px 1px 3px -1px",
    },
  },
  MuiCardHeader: {
    action: {
      marginTop: "-4px",
      marginRight: "-4px",
    },
  },
  MuiPickersDay: {
    day: {
      fontWeight: "300",
    },
  },
  MuiPickersYear: {
    root: {
      height: "64px",
    },
  },
  MuiPickersCalendar: {
    transitionContainer: {
      marginTop: "6px",
    },
  },
  MuiPickersCalendarHeader: {
    iconButton: {
      backgroundColor: "transparent",
      "& > *": {
        backgroundColor: "transparent",
      },
    },
    switchHeader: {
      marginTop: "2px",
      marginBottom: "4px",
    },
  },
  MuiPickersClock: {
    container: {
      margin: `32px 0 4px`,
    },
  },
  MuiPickersClockNumber: {
    clockNumber: {
      left: `calc(50% - 16px)`,
      width: "32px",
      height: "32px",
    },
  },
  MuiPickerDTHeader: {
    dateHeader: {
      "& h4": {
        fontSize: "2.125rem",
        fontWeight: 400,
      },
    },
    timeHeader: {
      "& h3": {
        fontSize: "3rem",
        fontWeight: 400,
      },
    },
  },
  MuiPickersTimePicker: {
    hourMinuteLabel: {
      "& h2": {
        fontSize: "3.75rem",
        fontWeight: 300,
      },
    },
  },
  MuiPickersToolbar: {
    toolbar: {
      "& h4": {
        fontSize: "2.125rem",
        fontWeight: 400,
      },
    },
  },
  MuiChip: {
    root: {
      borderRadius: "6px",
    },
  },
  MuiDialog: {
    paper: {
      padding: "1em",
    },
  },
  MuiButton: {
    containedPrimary: {
      backgroundColor: uoneLightBlue[500],
      borderRadius: "2em",
      border: "3px solid white",
      "&:hover": {
        backgroundColor: uoneLightGreen[500],
      },
    },
    outlinedPrimary: {
      borderRadius: "2em",
      border: `3px solid ${uoneLightBlue[500]}`,
      color: uoneLightBlue[500],
      "&:hover": {
        backgroundColor: uoneLightBlue[500],
        border: `3px solid ${uoneLightBlue[500]}`,
        color: "white",
      },
    },
  },
  MuiAvatar: {
    colorDefault: {
      backgroundColor: "black",
    },
  },
};

export default overrides;
