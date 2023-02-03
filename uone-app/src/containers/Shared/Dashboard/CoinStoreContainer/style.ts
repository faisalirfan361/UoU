import {makeStyles} from '@material-ui/core/styles';

const PADDING = 10;

const Styles = makeStyles((theme) => {
  return {
    container: {
      backgroundColor: theme.palette.background.paper,
      color: theme.palette.text.primary,
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      borderRadius: 10,
      padding: PADDING,
      border: `3px solid ${theme.palette.grey[100]}`
    },
    row: {
      display: 'flex',
      alignItems: 'center',
      marginTop: 25
    },
    item: {
      padding: 5
    },
    paginationContainer: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      marginTop: 25,
      marginBottom: 25
    }
  }
});

export default Styles;
