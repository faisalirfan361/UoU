import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles((theme) => {
  return {
    root: {
      padding: 0,
      margin: "unset !important",
      width: "100%",
    },
    titleContainer: {
      paddingBottom: 10,
    },
    cardRoot: {
      background: "#fff",
      maxWidth: 318,
      minHeight: 130,
      border: "none",
      borderRadius: 4,
      boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
      paddingTop: 0,
      marginBottom: theme.spacing(4),
    },
    buttonOutlined: {
      marginTop: theme.spacing(2),
    },
    cardContent: {
      padding: theme.spacing(2),
      paddingLeft: theme.spacing(2),
      paddingTop: 0,
      paddingBottom: 0,
      paddingRight: theme.spacing(2),
    },
    cardContentDivider: {
      marginBottom: theme.spacing(2),
    },
    subtitle1: {
      // fontFamily: 'Poppins',
      fontStyle: "normal",
      fontWeight: 500,
      fontSize: 16,
      color: "#171717",
      textAlign: "left",
      paddingTop: theme.spacing(1),
      paddingBottom: 0,
    },
    modalGrids: {
      "& .MuiGrid-item": {
        maxWidth: "100%",
        flexBasis: "100%",
      },
    },
    infiniteScroll: {
      overflow: "unset !important",
    },
    loading: {
      width: "100%",
      display: "flex",
      justifyContent: "center",
      padding: theme.spacing(1),
    },
    CircularProgress: {
      color: theme.common.uoneLightBlue500,
    },
    text: {
      width: "100%",
      display: "flex",
      justifyContent: "center",
      padding: theme.spacing(1),
    },
  };
});

export default Styles;
