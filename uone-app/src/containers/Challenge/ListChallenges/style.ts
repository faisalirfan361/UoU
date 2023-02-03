import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles((theme) => {
  return {
    root: {
      padding: theme.spacing(2),
      display: "flex",
      flexWrap: "wrap",
      width: "100%",
      margin: "unset !important",
    },
    buttonsSection: {
      justifyContent: "space-between",
      marginTop: theme.spacing(3),
      "& button": {
        marginRight: "10px",
      },
    },
    tableText: {
      fontWeight: 500,
      fontSize: "14px",
      lineHeight: "21px",
    },
    agentName: {
      fontWeight: 500,
      fontSize: "14px",
      lineHeight: "21px",
      color: "#2FB0D9",
    },
    challengeCard: {
      display: "flex",
      minWidth: 400,
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
    infiniteScroll: {
      overflow: "unset !important",
    },
  };
});

export default Styles;
