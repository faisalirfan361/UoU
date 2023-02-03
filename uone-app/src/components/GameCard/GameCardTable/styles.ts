import { makeStyles, darken } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  container: {
    maxHeight: 210,
    marginTop: "1em",
    overflow: 'auto',
  },
  stickyHeader: {
    backgroundColor: theme.palette.common.white,
    textTransform: "uppercase",
    fontWeight: 500,
    fontSize: "11px",
    lineHeight: "16px",
    padding: "1em",
  },
  agentCell: {
    display: "flex",
    alignItems: "center",
    padding: 0,
  },
  agentCellName: {
    display: "flex",
    alignItems: "center",
    padding: 0,
    color: theme.common?.uoneLightBlue[400],
    fontWeight: 500,
    fontSize: "14px",
    lineHeight: "21px",
    wordWrap: "break-word",
    flex: 1,
  },
}));
