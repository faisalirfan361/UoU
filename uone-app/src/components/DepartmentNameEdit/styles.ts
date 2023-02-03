import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  tableBodyTitle: {
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 14,
    color: "#2FB0D9",
    height: 16,
    overflow: "hidden",
    textOverflow: "ellipsis",
    lineClamp: 1,
  },
  editButton: {
    float: "right",
    width: 30,
    height: 30,
  },
}));

export default Style;
