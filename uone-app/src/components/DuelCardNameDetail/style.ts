import { makeStyles } from "@material-ui/core/styles";

export default  makeStyles((theme) => ({
  duelContainer: {
    width: "100%",
    boxShadow: "0px 1px 5px rgba(0, 0, 0, 0.2)",
    borderRadius: 3
  },
  duelHeader: {
    padding: theme.spacing(2),
    paddingBottom: theme.spacing(1),
    borderBottom: "1px solid #E1E1E1",
    alignItems:'center'
  },
  duelNameText: {
    fontSize: 18,
    color: `${theme.common.uoneLightBlue[400]}`,
  },
  duelStatusActive: {
    padding: '4px 10px',
    width: 'max-content',
    fontSize: 12,
    fontWeight: 500,
    color: `${theme.palette.common.white}`,
    backgroundColor: '#5AD787',
    borderRadius: 3,
    marginTop: theme.spacing(0.5),
  },
  duelCoinText: {
    color: `${theme.palette.common.black}`,
    fontSize: 16,
    paddingRight: theme.spacing(2),
    fontWeight: 600,
  },
  chipTextColor: {
    color: theme.palette.common.white,
  },
}));
