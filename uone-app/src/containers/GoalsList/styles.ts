import { makeStyles } from "@material-ui/core/styles";

export const useStyle = makeStyles((theme) => ({
  topCont: {
    width: "100%",
    marginBottom: theme.spacing(4),
    display: "flow-root"
  },
  createRoleButton: {
      width: 188,
      height: 40,
      backgroundColor: theme.palette.common.white,
      border: `2px solid ${theme.common.uoneLightBlue[400]}`,
      boxShadow: "0px 2px 10px rgba(0, 0, 0, 0.19)",
      borderRadius: 90,
      fontWeight: 600,
      fontSize: 13,
      lineHeight: 19,
      color: theme.common.uoneLightBlue[400],
      float: "right",
      textTransform: "capitalize",
  },
  goalContainer: {
    marginTop: theme.spacing(5),
    paddingTop: theme.spacing(5),
    borderTop: `1px solid ${theme.common.grey[400]}`,
  }
}));

