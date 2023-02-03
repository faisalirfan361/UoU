import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => {
  return {
    root: {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
    },
    container:{
      maxWidth: "100%",
      flexBasis: "100%"
    },
    loading: {
      width: "100%",
      display: "flex",
      justifyContent: "center",
        padding: "20px 0",
    },
    CircularProgress: {
      color: "#2FB0D9 !important",
    },
    infiniteScroll:{
      overflow:"unset !important"
    },
  };
});
