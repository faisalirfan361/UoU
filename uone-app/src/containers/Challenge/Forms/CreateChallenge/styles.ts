import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  form: {
    minWidth: "505px",
  },
  input: {
    flex: 1,
    margin: "5px 0",
  },
  inputContainer: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
    display: "flex",
    alignItems: "center",
  },
  gridHighlight: {
    background: theme.palette.common.white,
    boxSizing: "border-box",
    textTransform: "none",
    borderRadius: 4,
  },
  boxSelectHighlight: {
    paddingTop: 0,
  },
  typeRadio: {
    flexDirection: "row",
    "& label": {
      display: "inline-flex",
      alignItems: "center",
      marginRight: theme.spacing(1),
    },
  },
  filterWrapper: {
    justifyContent: "flex-end",
  },
  filterBox: {
    cursor: "pointer",
    alignItems: "center",
  },
  filterText: {
    marginLeft: 5,
    color: theme.common.uoneLightBlue[400],
  },
  filterIcon: {
    color: theme.common.uoneLightBlue[400],
  },
}));
