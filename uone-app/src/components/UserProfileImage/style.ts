import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    border: 'solid thin black',
    padding: 4,
    backgroundColor: theme.palette.common.white,
    '& img':{
      borderRadius: '50%'
    },
    '& svg':{
      color: '#9c9494'
    }
  }
}));

export default Style;
