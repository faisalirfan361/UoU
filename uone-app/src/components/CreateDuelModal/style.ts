import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  inputContainer: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(3),
    alignItems:'center'
  },
  typoText: {
    fontSize: '10.5pt',
    color: theme.palette.common.black,
  }
}));
