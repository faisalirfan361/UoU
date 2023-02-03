// @ts-nocheck

import { makeStyles } from "@material-ui/core/styles";

const Style = (props) =>
  makeStyles((theme) => ({
    cardRoot: {
      background: "#fff",
      maxWidth: 318,
      maxHeight: 80,
      border: "none",
      borderRadius: 3,
      boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
      paddingTop: 2,
      paddingRight: theme.spacing(1) / 2,
      paddingLeft: 0,
    },
    headerRoot: {
      alignItems: "normal",
    },
    headerRootAvatar: {
      width: 54,
      height: 54,
      padding: 2,
      borderRadius: "50%",
      background: "#252525",
      border: "1px solid #252525",
      backgroundClip: "content-box",
      marginTop: -6,
    },
    headerRootAction: {
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: 600,
      fontSize: 13,
      color: "#252525",
      marginTop: theme.spacing(1) / 2,
      verticalAlign: "middle",
    },
    headerRootActionPoints: {
      marginRight: theme.spacing(1) / 2,
      verticalAlign: "middle",
    },
    headerRootTitle: {
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: 500,
      fontSize: 11,
      color: "#9BABB3",
      textAlign: "left",
      lineHeight: 1,
      textTransform: "uppercase",
      marginLeft: -2,
    },
    headerRootSubtitle: {
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: "normal",
      fontSize: 16,
      color: "#000",
      textAlign: "left",
      lineHeight: 1,
      marginLeft: -2,
    },
  }));

export default Style;
