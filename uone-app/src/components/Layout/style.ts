import { makeStyles } from "@material-ui/core/styles";

const defaults = {
  fullWidth: "100%",
  fixedHeight: "120px",
  fontFamily: "sans-serif",
  menuBackgroundColor: "#333333",
  logoBackgroundColor: "#444444",
  navBackgroundColor: "#fefefe",
  menuItemsColor: "#cccccc",
  contentBackgroundColor: "whitesmoke",
  borderColor: "darkgrey",
};

const Styles = makeStyles((theme) => {
  return {
    root: {
      flexGrow: 1,
      overflow: "hidden",
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
    topSection: {
      maxHeight: defaults.fixedHeight,
      minHeight: defaults.fixedHeight,
    },
    logoContainer: {
      display: "flex",
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: defaults.logoBackgroundColor,
      color: "white",
      height: defaults.fixedHeight,
    },
    logoImg: {
      width: "75%",
    },
    navContainer: {
      width: defaults.fullWidth,
      padding: "2%",
      backgroundColor: defaults.navBackgroundColor,
      border: `1px solid ${defaults.borderColor}`,
      fontFamily: defaults.fontFamily,
      display: "flex",
      flexDirection: "column",
      justifyContent: "center",
      textAlign: "left",
      color: "#171717",
      maxHeight: defaults.fixedHeight,
      minHeight: defaults.fixedHeight,
    },
    menuContainer: {
      width: defaults.fullWidth,
      backgroundColor: defaults.menuBackgroundColor,
      height: `calc(100vh - ${defaults.fixedHeight})`,
      color: defaults.menuItemsColor,
      fontFamily: defaults.fontFamily,
      display: "flex",
      flexDirection: "column",
    },
    contentContainer: {
      width: defaults.fullWidth,
      backgroundColor: defaults.contentBackgroundColor,
      height: `calc(100vh - ${defaults.fixedHeight})`,
      border: `1px solid ${defaults.borderColor}`,
      borderTop: "0px",
      fontFamily: defaults.fontFamily,
      padding: "0 100px",
      paddingTop: theme.spacing(6),
      overflow: "scroll",
      textAlign: "left",
      color: "#171717",
    },
    menuIcon: {
      paddingRight: "18px",
      color: "inherit",
      minWidth: "auto",
      lineHeight: "1em",
      fontSize: "24px",
    },
    menuLabel: {
      color: "inherit",
      fontWeight: "inherit",
    },
    menuItem: {
      padding: "15px 20px",
      display: "flex",
      alignItems: "center",
      borderLeft: "6px solid transparent",
      cursor: "pointer",
      color: defaults.menuItemsColor,
      "&:hover": {
        background: "rgba(255,255,255,0.05)",
      },
    },
    menuItemActive: {
      color: "#fff",
      borderLeft: "6px solid #2FB0D9",
      background: "rgba(255,255,255,0.1)",
      "&:hover": {
        background: "rgba(255,255,255,0.1)",
      },
      "& .MuiTypography-body1": {
        fontWeight: 500,
      },
    },
    signOutItem: {
      marginTop: "auto",
      "& > a": {
        display: "flex",
        alignItems: "center",
      },
    },
    navRightSection: {
      display: "flex",
      alignItems: "center",
    },
    searchInput: {
      width: defaults.fullWidth,
      backgroundColor: `${defaults.navBackgroundColor} !important`,
      color: "black !important",
      border: `1px solid ${defaults.menuItemsColor}`,
    },
    inputBase: {
      color: defaults.menuBackgroundColor,
    },
    notificationIconContainer: {
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
    },
  };
});

export default Styles;
