import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    padding: `${theme.spacing(3)}px ${theme.spacing(3)}px 0px ${theme.spacing(3)}px`,
    backgroundColor: "white",
    width:549,
    margin: '0px auto 20px auto',
    boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.25)',
  },
}));

export default Style;
