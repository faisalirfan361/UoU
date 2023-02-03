import { makeStyles } from "@material-ui/core/styles";
import theme from "theme";

export default makeStyles((theme) => ({
  root: {
    padding: theme.spacing(4, 0, 2, 0),
  },
  gridContainer: {
    paddingBottom: theme.spacing(2),
  },
  formulaInput: {},
  operandButton: {
    border: `2px solid ${theme.common?.uoneLightBlue[600]}`,
    borderRadius: "20px",
    width: "80px",
    height: theme.spacing(5),
    cursor: "pointer",
  },
  clearButton: {
    margin: "4px 0px",
    padding: "10px 10px 10px 0px;",
    fontSize: 14,
    fontWeight: 600,
    color: theme.common?.uoneLightBlue[400],
    cursor: "pointer",
    display: "inline-block",
  },
}));
