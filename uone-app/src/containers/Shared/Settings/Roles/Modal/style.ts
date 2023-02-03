import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  form: {
    width: "95%",
  },
  input: {
    flex: 1,
    margin: "5px 0",
  },
  inputContainer: {
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1),
    display: "flex",
  },
  gridHighlight: {
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),
    paddingTop: theme.spacing(1) / 2,
    paddingBottom: theme.spacing(1) / 2,
    background: theme.palette.common.white,
    boxSizing: "border-box",
    textTransform: "none",
    borderRadius: 4,
  },
  boxSelectHighlight: {
    padding: theme.spacing(1),
    paddingBottom: theme.spacing(1) + 2,
  },
  typeRadio: {
    flexDirection: "row",
    "& label": {
      display: "inline-flex",
      alignItems: "center",
      marginRight: theme.spacing(1),
    },
  },
}));
