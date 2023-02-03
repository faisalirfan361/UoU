import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles(() => {
  return {
    actions: {
      height: 80,
      paddingTop: 0,
      position: "relative",
    },
    modalGrids: {
      "& .MuiGrid-item": {
        maxWidth: "100%",
        flexBasis: "100%",
      },
    },
    closeButton: {
      position: "absolute",
      right: 11,
      top: 17,
    },
    userInfoContainer: {
      display: "inline-block",
      width: "100%",
    },
    avatar: {
      height: 53,
      width: 53,
      display: "inline-block",
    },
    userName: {
      size: 22,
      fontWeight: 500,
      position: "relative",
      top: -13,
      paddingLeft: 10,
    },
    dividerContainer: {
      margin: "1px -1em",
    },
    divider: {
      width: "100%",
    },
    userAvatar: {
      width: 53,
      height: 53,
    },
  };
});

export default Styles;
