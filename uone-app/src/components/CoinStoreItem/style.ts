import { makeStyles } from "@material-ui/core/styles";

const PADDING = 10;
const GAP = 15;
const MAX_LINES = 6;
const MAX_HEIGHT = "320px";

const Styles = makeStyles((theme) => {
  return {
    container: {
      backgroundColor: theme.palette.background.paper,
      color: theme.palette.text.primary,
      display: "flex",
      flexDirection: "column",
      justifyContent: "space-between",
      alignItems: "center",
      borderRadius: 10,
      padding: PADDING,
      border: `3px solid ${theme.palette.grey[100]}`,
      minHeight: MAX_HEIGHT,
      maxHeight: MAX_HEIGHT,
    },
    row: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      gap: GAP,
    },
    title: {
      fontSize: theme.typography.subtitle1.fontSize,
    },
    description: {
      fontSize: "0.75rem",
      lineHeight: "1rem",
      minHeight: `calc(1rem * ${MAX_LINES})`,
      height: `calc(1rem * ${MAX_LINES})`,
      maxHeight: `calc(1rem * ${MAX_LINES})`,
      overflow: "hidden",
      margin: "20px 0px",
    },
    points: {
      fontSize: theme.typography.h6.fontSize,
    },
    sup: {
      fontSize: theme.typography.body1.fontSize,
    },
    coinStoreThumbnail: {
      width: "80px",
      height: "50px",
    },
  };
});

export default Styles;
