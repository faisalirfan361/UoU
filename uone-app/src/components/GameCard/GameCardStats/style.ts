import { makeStyles, darken } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  label: {
    fontWeight: 500,
    fontSize: "11px",
    lineHeight: "16px",
    color: theme.common.grey[600],
    width: "100%",
  },
  value: {
    fontWeight: 600,
    fontSize: "14px",
    lineHeight: "21px",
    width: "100%",
  },
}));
