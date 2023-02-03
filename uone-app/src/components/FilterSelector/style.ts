import { makeStyles } from "@material-ui/core/styles";

export const Style = makeStyles((theme) => ({
  root:{
    padding: 0,
  },
  selectorLabel: {
    width:200,
    position: "relative",
    fontSize: 16,
    fontWeight: 500,
    top: 6,
  },
}));

export const autoCompleteStyle = makeStyles((theme) => ({
  root: {
    marginLeft: 10,
    width: 180,
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
