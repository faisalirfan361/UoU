import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  tabs: {
    padding: '0px 20px',
    paddingBottom: '20px',
    "& .MuiTab-wrapper": {
      color: theme.palette.text.primary,
      textTransform: 'initial',
      width: 100
    },
    "& .Mui-selected": {
      fontWeight: 'bold',
    }
  },
  tab: {
    minWidth: 50,
    width: 50,
    padding: 0
  },
  paper: {
    padding: 25,
    width: '100%'
  }
}));

export default Style;
