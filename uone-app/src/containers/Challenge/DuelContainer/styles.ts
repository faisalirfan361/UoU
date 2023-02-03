import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  duelContainer: {
    justifyContent: "space-between",
    marginTop: theme.spacing(3),
  },
  createDuelButtonContainer: {
    float: "right",
  },
  DuelCardContainer: {
    padding: `${theme.spacing(2)}px ${theme.spacing(0)}px`,
  },
  duelTextContainer: {
    marginTop: theme.spacing(11),
    textAlign: "center",
  },
  duelText: {
    fontSize: "15pt",
    fontFamily: "Roboto",
    fontWeight: 300,
    color: `${theme.palette.common.black}`,
  },
  duelTextSpan: {
    color: `${theme.common.uoneLightBlue[500]}`,
    fontWeight: 500,
  },
  infiniteScroll: {
    overflow: "unset !important",
  },
  loading: {
    width: "100%",
    display: "flex",
    justifyContent: "center",
    padding: theme.spacing(1),
  },
  CircularProgress: {
    color: theme.common.uoneLightBlue[500],
  },
}));
