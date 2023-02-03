import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles((theme: any) => {
  return {
    container: {
      padding: "20px 0",
    },
    form: {
      width: "100%",
    },
    formLabel: {
      fontSize: "16px",
      fontWeight: theme.typography.fontWeightMedium,
      display: "block",
      margin: "10px 0",
    },
    formSelect: {
      all: "unset",
      fontSize: theme.typography.fontSize,

      "&::after": {
        content: "some content",
        display: "block",
        height: 60,
        marginTop: -60,
      },
    },
    formSelectCaret: {
      paddingLeft: "10px",
    },
    divider: {
      width: "100%",
      height: "1px",
      backgroundColor: theme.palette.secondary.main,
      margin: "25px 0",
    },
    pointsInputContainer: {
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
    },
    pointsInput: {
      width: "165px",
      height: "52px",
      borderRadius: "3px",
      border: "1px solid rgba(15, 44, 67, 0.495903)",
      marginLeft: "10px",
      marginRight: "10px",
      textAlign: "center",

      "&::placeholder": {
        color: "#252525",
      },
    },
    budgetInputContainer: {
      padding: "16px 0",
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
    },
    budgetAmountInput: {
      width: "165px",
      height: "52px",
      borderRadius: "3px",
      border: "1px solid rgba(15, 44, 67, 0.495903)",
      textAlign: "center",

      "&::placeholder": {
        color: "#252525",
      },
    },
    budgetRecurrenceInput: {
      all: "unset",
      fontSize: theme.typography.fontSize,
      marginLeft: "10px",
    },
  };
});

export default Styles;
