import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    // @ts-ignore
    zIndex: theme.zIndex.loading,
    position: "absolute",
    background: "rgba(0, 0, 0, 0.87)",
    flex: 1,
    display: "flex",
    margin: 0,
    padding: theme.spacing(3),
    width: "100%",
    maxWidth: "100%",
    height: "100vh",
    alignSelf: "center",
    alignItems: "center",
    justifyContent: "center",
  },
  progressContainer: {
    width: 110,
    height: 110,
    borderRadius: "50%",
    background: "#ff5722",
    border: "3px solid #ff9800",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
}));

export default Style;
