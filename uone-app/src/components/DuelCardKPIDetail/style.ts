import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  KpiDurationHeading: {
    fontSize: 11,
    color: theme.common.grey[900],
    fontWeight: 400
  },
  KpiDurationData: {
    fontSize: 14,
    color: `${theme.palette.common.black}`,
    fontWeight: 400
  },
  KpiDurationDataBold: {
    fontSize: 14,
    color: `${theme.palette.common.black}`,
    fontWeight: 600
  }
}));
