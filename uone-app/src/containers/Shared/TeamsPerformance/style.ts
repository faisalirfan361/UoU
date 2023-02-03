import { makeStyles, Theme } from "@material-ui/core/styles";
import { unset } from "lodash";

export const Style = makeStyles((theme) => ({
  root: {
    marginTop: theme.spacing(0),
  },
  avatar: {
    width: 54,
    height: 54,
    padding: 2,
    borderRadius: "50%",
    background: theme.common.grey[900],
    border: `1px solid ${theme.common.grey[900]}`,
    backgroundClip: "content-box",
    marginTop: -6,
  },
  subtitle1: {
    // fontFamily: 'Poppins',
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 16,
    color: theme.common.grey[900],
    textAlign: "left",
  },
  subtitle2: {
    // fontFamily: 'Poppins',
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 14,
    color: theme.palette.primary.main,
    textAlign: "right",
    cursor: "pointer",
  },
  buttonOutlined: {
    marginTop: theme.spacing(2),
  },
  autocomplete: {
    width: 300,
  },
  dropdownSection: {
    borderBottom: `solid thin ${theme.common.grey[400]}`,
    paddingBottom: 10,
    marginBottom: 40,
  },
  dropdownLabel: {
    paddingRight: 20,
    position: "relative",
    fontSize: 16,
    fontWeight: 500,
    top: 6,
  },
  teamPerformance: {
    padding: 0,
  },
  infiniteScroll:{
    overflow:"unset !important"
  },
  containDeps: {
    width: "100%",
    margin: "unset !important",
  },
  loading: {
    width: "100%",
    display: "flex",
    justifyContent: "center",
    padding: theme.spacing(1),
  },
  CircularProgress: {
    color: theme.common.uoneLightBlue[500],
  }
}));

export const autoCompleteStyle = makeStyles((theme) => ({
  root: {
    width: 200,
    display: "inline-block",
    "& .MuiInput-underline:before": {
      border: "none",
    },
    "& .MuiFormLabel-root": {
      color: theme.common.uoneLightBlue[500],
      fontWeight: 500,
    },
    "& svg": {
      color: theme.common.uoneLightBlue[500],
    },
    "& input": {
      color: theme.common.uoneLightBlue[500],
      fontWeight: 500,
    },
    "& .MuiInputLabel-formControl": {
      top: -16,
    },
    "& .MuiAutocomplete-hasPopupIcon.MuiAutocomplete-hasClearIcon .MuiAutocomplete-inputRoot":
      {
        marginTop: 0,
      },
    "& label + .MuiInput-formControl": {
      marginTop: 0,
    },
  },
}));
