import {makeStyles} from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  itemCard: {
    display: 'flex'
  },
  details: {
    display: 'flex',
    alignItems: 'center',
  },
  content: {
    flex: '1 0 auto',
  },
  mainImage: {
    width: 200,
    height: 126,
    marginLeft: theme.spacing(2),
    marginTop: theme.spacing(2),
    marginRight: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },
  redeemContainer: {
    display: 'flex',
    alignItems: 'flex-end',
    marginLeft: theme.spacing(3),
    marginBottom: theme.spacing(3),
    marginRight: theme.spacing(3)
  },
  description: {
    marginTop: theme.spacing(2),
    fontSize: '7pt'
  }
}));

export default Style;
