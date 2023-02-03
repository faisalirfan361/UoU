import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  challengeName: {
    fontSize: 18,
    fontWeight: 500,
    color: theme.common.uoneLightBlue[500],
    marginBottom: theme.spacing(2),
  },
  coins: {
    fontSize: 16,
    fontWeight: 600,
  },
  activeState: {
    padding: theme.spacing(1),
    backgroundColor: theme.common.uoneLightGreen[500],
    display: "inline-block",
    borderRadius: "3px",
    color: theme.palette.common.white,
    fontSize: 12,
    fontWeight: 500,
  },
  expiredState: {
    padding: theme.spacing(1),
    backgroundColor: theme.palette.grey[500],
    display: "inline-block",
    borderRadius: "3px",
    color: theme.palette.common.white,
    fontSize: 12,
    fontWeight: 500,
  },
  futureStart: {
    padding: theme.spacing(1),
    backgroundColor: theme.palette.grey[200],
    display: "inline-block",
    borderRadius: "3px",
    color: theme.palette.common.black,
    fontSize: 12,
    fontWeight: 500,
  },
  editIcon: {
    border: `solid 2px ${theme.common.uoneLightBlue[500]}`,
    color: theme.common.uoneLightBlue[500],
    padding: 5,
    float: "right",
  },
  deleteIcon: {
    border: `solid 2px ${theme.common.uoneShineViolet[500]}`,
    color: theme.common.uoneShineViolet[500],
    padding: 5,
    float: "right",
    marginLeft: theme.spacing(1),
  },
}));
