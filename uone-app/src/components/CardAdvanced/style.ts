// @ts-nocheck

import { makeStyles } from "@material-ui/core/styles";

const Style = (props) =>
  makeStyles((theme) => ({
    cardRoot: {
      background: "#fff",
      maxWidth: 318,
      minHeight: 130,
      border: "none",
      borderTop: `10px solid ${props.statusColor}`,
      borderRadius: 4,
      boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
      paddingTop: 2,
      paddingRight: theme.spacing(1),
    },
    headerRoot: {
      padding: 0,
      paddingTop: theme.spacing(1),
      paddingRight: theme.spacing(2),
      paddingBottom: theme.spacing(1),
    },
    headerRootAvatar: {
      marginRight: -theme.spacing(1),
      marginLeft: -theme.spacing(1),
    },
    headerRootAction: {
      position: "sticky",
      right: 0,
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: 600,
      fontSize: 13,
      color: "#5AD787",
      marginTop: theme.spacing(7),
    },
    headerRootTitle: {
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: 500,
      fontSize: 11,
      color: "#9BABB3",
      textAlign: "left",
      lineHeight: 1,
      marginLeft: -2,
      textTransform: "uppercase",
    },
    headerRootSubtitle: {
      // fontFamily: "Poppins",
      fontStyle: "normal",
      fontWeight: "normal",
      fontSize: 16,
      color: "#000",
      textAlign: "left",
      marginLeft: -2,
      lineHeight: 1,
    },
    cardContent: {
      padding: theme.spacing(1),
      paddingLeft: theme.spacing(2),
      paddingTop: 0,
      paddingBottom: 0,
    },
    cardContentDivider: {
      marginBottom: theme.spacing(2),
    },
  }));

export default Style;
