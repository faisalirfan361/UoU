import { darken, makeStyles } from '@material-ui/core/styles';

const PADDING = 20;
const GAP = 15;

const Styles = makeStyles((theme) => {
  return {
    container: {
      backgroundColor: '#8A9DB3',
      color: 'white',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      borderRadius: 10,
      height: 80
    },
    flexStart: {
      display: 'flex',
      justifyContent: 'start',
      alignItems: 'center',
      gap: GAP,
      padding: PADDING,
      maxHeight: 80,
      height: 80
    },
    boldText: {
      fontWeight: 'bold'
    },
    text: {
      textAlign: 'left'
    },
    bordered: {
      borderRight: `2px solid ${darken('#8A9DB3', 0.1)}`
    }
  }
});

export default Styles;
