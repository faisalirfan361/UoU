import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  dropzone: {
    border: `2px dashed ${theme.common.uoneLightBlue[500]}`,
    width: "100%",
    borderRadius: "8px",
    padding: "1em 1em",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    cursor: "Pointer",
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },
  dropzoneTitle: {
    color: theme.common.uoneLightBlue[500],
    lineHeight: "1rem",
  },
  dropzoneIcon: {
    fontSize: "8rem",
    lineHeight: "1rem",
    color: theme.common.uoneLightBlue[500],
  },
  uploadingWrapper: {
    padding: theme.spacing(2),
  },
  textRequirement:{
    margin: 0,
    fontWeight: 600,
    textAlign: "center"
  }
}));
