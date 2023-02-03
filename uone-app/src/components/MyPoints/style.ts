import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  container: {
    margin: "0 auto",
    width: "80%",
  },
  title: { fontSize: 32, color: "#ffffff", lineHeight: 1 },
  subtitle: { fontSize: 12, color: "#444444", lineHeight: 1 },
  containerTop: {
    width: "100%",
    display: "flex",
    justifyContent: "center",
  },
  semiCircleTop: {
    display: "inline-block",
    width: 160,
    height: 80,
    borderTopLeftRadius: 80,
    borderTopRightRadius: 80,
    background: "#82c341",
    color: "white",
    textAlign: "center",
    paddingTop: 24,
  },
  containerBottom: {
    width: "100%",
    display: "flex",
    justifyContent: "center",
  },
  semiCircleBottom: {
    display: "inline-block",
    width: 160,
    height: 80,
    borderBottomLeftRadius: 80,
    borderBottomRightRadius: 80,
    background: "#fdb515",
    color: "white",
    textAlign: "center",
    paddingTop: 10,
  },
}));

export default Style;
