import { makeStyles } from "@material-ui/core/styles";
import { common } from "@material-ui/core/colors";

const Style = makeStyles((theme) => ({
  winnerRow: {
    "& td.MuiTableCell-root": {
      backgroundColor: `${theme.common.uoneLightGreen[500]}`,
      color: `${theme.palette.common.white}`,
      "& p": {
        color: `${theme.palette.common.white}`,
      },
    },
  },
  rankColumn: {
    width: "15%",
  },
  agentNameColumn: {
    width: "60%",
  },
  pointsColumn: {
    width: "25%",
  },
  agentName: {
    paddingLeft: theme.spacing(2),
    position: "relative",
    top: theme.spacing(1),
    color: theme.common.uoneLightBlue[500],
  },
  avatar: {
    width: 40,
    height: 40,
  },
}));

export default Style;
