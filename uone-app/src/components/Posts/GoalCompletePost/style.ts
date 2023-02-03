import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    padding: `${theme.spacing(2)}px ${theme.spacing(2)}px 0px ${theme.spacing(2)}px`,
    backgroundColor: theme.palette.common.white,
    width:549,
    margin: '0px auto 20px auto',
    boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.25)',

  },

}));

export default Style;
