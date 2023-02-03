import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme: any) => {
  return {
    container: {
      padding: `${theme.spacing(3)}px 0px`,
    },
    form: {
      width: "100%",
    },
    HeadingWrapper: {
      marginTop: theme.spacing(4),
      marginBottom: theme.spacing(1),
    },
    formHeading: {
      fontSize: 16,
      fontWeight: theme.typography.fontWeightMedium,
      margin: `${theme.spacing(1.25)}px 0px`,
    },
    formLabel: {
      fontSize: 14,
      fontWeight: theme.typography.fontWeightRegular,
      margin: `${theme.spacing(1.25)}px 0px`,
      alignItems: "center",
    },
    formSelect: {
      all: "unset",
      fontSize: 16,
      "&::after": {
        content: "some content",
        display: "block",
        height: 60,
        marginTop: -60,
      },
    },
    paymentContainer: {
      marginTop: theme.spacing(6),
    },
    formSelectCaret: {
      paddingLeft: theme.spacing(1.25),
    },
    divider: {
      width: "100%",
      height: 1,
      backgroundColor: theme.palette.grey[600],
      margin: `${theme.spacing(3)}px 0px`,
    },
    pointsInputContainer: {
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
      height: "max-content",
    },
    pointsInput: {
      borderRadius: 3,
      marginLeft: theme.spacing(1.25),
      marginRight: theme.spacing(1.25),
      textAlign: "center",

      "&::placeholder": {
        color: "#252525",
      },
      "& input[type=number]": {
        "-moz-appearance": "textfield",
      },
      "& input[type=number]::-webkit-outer-spin-button": {
        "-webkit-appearance": "none",
        margin: 0,
      },
      "& input[type=number]::-webkit-inner-spin-button": {
        "-webkit-appearance": "none",
        margin: 0,
      },
    },
    budgetInputContainer: {
      padding: `${theme.spacing(2)}px 0px`,
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
    },
    budgetRecurrenceInput: {
      all: "unset",
      fontSize: 16,
      marginLeft: theme.spacing(1.25),
    },
  };
});
