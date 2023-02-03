import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles((theme) => {
  return {
    container: {
      padding: "20px 0"
    },
    formSelectCaret: {
      paddingLeft: "10px"
    },
    createRoleButtonContainer: {
      padding: "30px 0",
      justifyContent: "flex-end"
    },
    createRoleButton: {
      background: "yellow",
      width: "188px",
      height: "40px",
      backgroundColor: "#2FB0D9",
      border: "3px solid #FFFFFF",
      boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
      borderRadius: "90px",
      fontWeight: 600,
      fontSize: "13px",
      lineHeight: "19px",
      color: "white",
      float: "right",
      textTransform: "capitalize"
    },
    tableHeaderContainer: {
      padding: "10px 0px"
    },
    tableHeader: {
      textAlign: "center",
      fontWeight: 500,
    },
    tableRow: {
      height: "71px",
      backgroundColor: "transparent",
      borderTop: "1px solid #D1D7DA",
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      textAlign: "center",

      "&:last-child": {
        borderBottom: "1px solid #D1D7DA",
      }
    },
    iconContainer: {
      borderLeft: "1px solid #D1D7DA",
      height: "71px",
      lineHeight: "71px",
      cursor: "pointer",

      "&:last-child": {
        borderRight: "1px solid #D1D7DA",
      }
    },
    roleName: {
      textAlign: "left",
      paddingLeft: "10px"
    },
    roleSelect: {
      all: "unset",
      fontSize: "24px",

      "&::after": {
        content: "some content",
        display: "block",
        height: 60,
        marginTop: -60
      }
    },
    onRoleIcon: {
      fontSize: "30px",
      color: "#2FB0D9",
      height: "71px"
    },
    offRoleIcon: {
      fontSize: "30px",
      color: "#8B9BA3",
      height: "71px"
    },
    adminActions: {
      display: "flex",
      justifyContent: "flex-end",
    },
  }
});

export default Styles;
