import { makeStyles } from "@material-ui/core/styles";
import theme from "theme";

export default makeStyles((theme) => ({
  popupContainer: {
    padding: theme.spacing(2, 3, 3, 3),
  },
  selectRowConatainer: {
    paddingTop: theme.spacing(4),
    display: "flex",
    marginLeft: theme.spacing(1),
  },

  andContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    border: `1px solid ${theme.common?.grey[400]}`,
    borderLeft: "none",
  },
  andWrapper: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  operationContainer: {
    display: "flex",
    justifyContent: "space-between",
    marginLeft: theme.spacing(5),
  },

  metricFieldWrapper: {
    marginTop: theme.spacing(3.5),
    border: `1px solid ${theme.common?.grey[400]}`,
    padding: `${theme.spacing(2)}px ${theme.spacing(2.5)}px`,
    minHeight: theme.spacing(6.5),
  },
  metricFieldText: {
    color: theme.common?.grey[900],
    fontSize: 16,
  },

  icon: {
    color: theme.common?.uoneLightBlue[600],
    cursor: "pointer",
  },
  btnCancel: {
    marginRight: theme.spacing(2),
  },
  cancelButton: {
    color: theme.common?.grey[600],
    fontWeight: 500,
    cursor: "pointer",
    fontSize: 14,
    marginRight: theme.spacing(4),
  },
  editButtonSection: {
    display: "inline",
    float: "right",
    paddingTop: theme.spacing(2),
  },
  flipText: {
    color: theme.common?.grey[600],
    fontWeight: 500,
    fontSize: 14,
    marginRight: theme.spacing(2),
  }
}));
