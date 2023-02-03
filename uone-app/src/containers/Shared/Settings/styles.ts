import { makeStyles } from '@material-ui/core/styles';

export default makeStyles((theme) => {
  return {
    tabs: {
      "& .MuiTab-wrapper": {
        color: theme.palette.text.primary,
        textTransform: 'initial'
      },
      "& .Mui-selected": {
        fontWeight: 'bold',
      }
    },
    tabsContainer: {
    },
  }
});
