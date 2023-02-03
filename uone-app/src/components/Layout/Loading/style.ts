import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    width: '100%',
    '& > * + *': {
      marginTop: theme.spacing(1),
    },
    height: 0,
    overflow: 'hidden',
    transition: '0.3s all ease'
  },
  open: {
    height: '1.2em',
  },
  colorPrimary: {
    backgroundColor: '#FCD248'
  },
  barColorPrimary: {
    backgroundColor: '#FCD248'
  },
  colorSecondary: {
    backgroundColor: '#EF647B'
  },
  barColorSecondary: {
    backgroundColor: '#EF647B'
  },
}));

export default Style;
