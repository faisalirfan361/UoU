import { makeStyles } from "@material-ui/core/styles";

export default  makeStyles((theme) => ({
  duelContainer: {
    width: "100%",
    boxShadow: "0px 1px 5px rgba(0, 0, 0, 0.2)",
    borderRadius: 3
  },
  duelHeader: {
    padding: `${theme.spacing(2)}px ${theme.spacing(1)}px`,
    borderBottom: "1px solid #E1E1E1",
    alignItems:'center'
  },
  duelNameText: {
    fontSize: 18,
    color: `${theme.common.uoneLightBlue[400]}`,
  },
  duelStatusActive: {
    fontSize: 14,
    fontWeight: 500,
    color: `${theme.palette.common.white}`,
    backgroundColor: '#5AD787',
    borderRadius: 5,
    marginTop: theme.spacing(0.5),
  },
  duelCoinText: {
    color: `${theme.palette.common.black}`,
    fontSize: 16,
    paddingRight: theme.spacing(2),
    fontWeight: 600,
  },
  KpiDurationHeading: {
    fontSize: 11,
    color: theme.common.grey[900],
    fontWeight: 500
  },
  KpiDurationData: {
    fontSize: 14,
    color: `${theme.palette.common.black}`,
    fontWeight: 600
  },
  KpiDurationDataBold: {
    fontSize: 14,
    color: `${theme.palette.common.black}`,
    fontWeight: 600
  },
  KpiHeading: {
    textAlign: 'center',
    fontSize: 10,
  },
  KpiDuration: {
    textAlign: 'center',
  },
  KpiDurationContainer: {
    marginTop: 20,
  }
}));
