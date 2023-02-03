import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  itemCard: {
    display: 'flex',
  },
  details: {
    display: 'flex',
    flexDirection: 'column',
  },
  content: {
    flex: '1 0 auto',
    paddingBottom: `${theme.spacing(2)}px !important`,
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
    marginBottom: theme.spacing(2),
    marginRight: theme.spacing(3)
  },
  description: {
    marginTop: theme.spacing(2),
    fontSize: '7pt'
  },
  labelPlacementStart: {
    marginLeft: 1,
    paddingTop: "30px",
  },
  label: {
    fontSize:"15px"
  }
}));
