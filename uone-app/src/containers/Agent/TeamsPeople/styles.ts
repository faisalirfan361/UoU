import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    //
  },
  visuallyHidden: {
    border: 0,
    clip: "rect(0 0 0 0)",
    height: 1,
    margin: -1,
    overflow: "hidden",
    padding: 0,
    position: "absolute",
    top: 20,
    width: 1,
  },
  row: {
    height: 70,
    maxHeight: 70,
  },
  rowEven: {
    background: "transparent",
  },
  rowOdd: {
    background: "#fff",
  },
  tableCountTitle: {
    fontStyle: "normal",
    fontWeight: "normal",
    fontSize: 20,
    color: "#252525",
  },
  tableCellRoot: {
    textAlign: "left",
  },
  tableHeaderTitle: {
    textTransform: "uppercase",
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 11,
    color: "#171717",
  },
  tableBodyTitle: {
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 14,
    color: "#2FB0D9",
  },
  tableBodySubtitle: {
    fontStyle: "normal",
    fontWeight: "normal",
    fontSize: 14,
    color: "#000",
  },
  tableDivider: {
    color: "#8B9BA3",
    marginBottom: theme.spacing(3),
  },
  nameGrid: {
    maxWidth: 320,
    border: "1px solid red",
  },
}));

export default Style;
