import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => {
  return {
    root: {
      paddingBottom: theme.spacing(1),
    },
    fullScreenContainer: {
      backgroundColor: theme.palette.common.white,
      width: "100%",
      padding: theme.spacing(7, 7),
      overflowY: "scroll",
    },
    logoContainer: {
      marginBottom: theme.spacing(5),
    },
    leaderboardTitle: {
      fontSize: 16,
      fontWeight: 400,
      marginBottom: theme.spacing(2),
    },
    logo: {
      width: 200,
    },
  };
});

