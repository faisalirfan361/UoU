import { makeStyles } from '@material-ui/core/styles';

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
      marginTop: 25,
    },
    item: {
      padding: 5
    },
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
      borderBottom: `1px solid ${theme.palette.text.secondary}`,
      paddingTop: 50
    },
    chartContainer: {
      margin: '50px 0px',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center'
    },
    pointLeadersItem: {
      padding: 10
    },
    title: {
      fontSize: '16px',
      fontStyle: 'normal',
      fontWeight: 500,
      lineHeight: '24px',
      letterSpacing: 0,

    },
    teamPerformanceSection:{
      paddingBottom: theme.spacing(10),
    }
  }
});

export default Styles;
