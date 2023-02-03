import { makeStyles } from "@material-ui/core/styles";

const PADDING = 20;
const GAP = 15;

const Styles = makeStyles((theme) => {
  return {
    backgroundImage: {
      width: "100%",
      display: "flex",
      backgroundSize: "cover !important",
      backgroundRepeat: "no-repeat !important",
      height: 150,
      padding: 10,
      position: "relative",
    },
    container: {
      backgroundColor: "#364556",
      color: "white",
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      borderRadius: 3,
      height: 80,
    },
    flexStart: {
      display: "flex",
      justifyContent: "start",
      alignItems: "center",
      gap: GAP,
      padding: PADDING,
      maxHeight: 80,
      height: 80,
    },
    boldText: {
      fontWeight: "bold",
    },
    modalcontent: {
      top: "50%",
      left: "50%",
      right: "auto",
      bottom: "auto",
      marginRight: "-50%",
      transform: "translate(-50%, -50%)",
      backgroundColor: "#FFF",
      position: "absolute",
      padding: "30px",
      borderRadius: "6px",
      border: "3px solid",
    },
    modalClose: {
      position: "absolute",
      cursor: "pointer",
      top: "7px",
      right: "7px",
      borderRadius: "15px",
      fontWeight: "bold",
      color: "#ccc",
      border: "none",
      background: "#000",
    },
    msgImageUpload: {
      marginTop: "30px",
    },
    text: {
      textAlign: "left",
    },
    count: {
      fontWeight: 600,
      fontSize: "16px",
      lineHeight: "24px",
    },
    countLabel: {
      fontWeight: 500,
      fontSize: "12px",
      lineHeight: "18px",
    },
  };
});

export default Styles;
