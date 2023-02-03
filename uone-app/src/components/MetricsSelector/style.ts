import { makeStyles } from "@material-ui/core/styles";

export const useMetricSelectorStyles = makeStyles((theme) => ({
  root: {
    padding: 0,
  },
}));

export const autoCompleteStyle = makeStyles((theme) => ({
  root: {
    border: `1px solid ${theme.common?.grey[400]}`,
    borderRadius: "3px",
    padding: "10px",
    margin: 0,
    display: "inline-block",
    "& .MuiInput-underline:before": {
      border: "none",
    },
    "& .MuiFormLabel-root": {
      color: theme.common?.uoneLightBlue[500],
      fontWeight: 500,
    },
    "& svg": {
      color: theme.common?.uoneLightBlue[500],
    },
    "& input": {
      color: theme.common?.grey[500],
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
