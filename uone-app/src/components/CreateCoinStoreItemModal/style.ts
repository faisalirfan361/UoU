import { makeStyles } from "@material-ui/core/styles";

 export default makeStyles((theme) => ({
  itemCard: {
    display: 'flex'
  },
  details: {
    display: 'flex',
    flexDirection: 'column',
  },
  content: {
    flex: '1 0 auto',
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
    marginBottom: theme.spacing(3),
    marginRight: theme.spacing(3)
  },
  description: {
    marginTop: theme.spacing(2),
    fontSize: '7pt'
  },
  fieldClass: {
    marginBottom: '20px'
  },
  fieldBox: {
    width: '300px'
  },
  fileField: { 
    width: '200px'
  },
  dropzone: {
    border: `2px dashed ${theme.common?.uoneLightBlue[500]}`,
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
    maxWidth: "400px"
  },
  dropzoneTitle: {
    color: theme.common?.uoneLightBlue[500],
    lineHeight: "1rem",
  },
  dropzoneIcon: {
    fontSize: "8rem",
    lineHeight: "1rem",
    color: theme.common?.uoneLightBlue[500],
  },
  textRequirement:{
    margin: 0,
    fontWeight: 600,
    textAlign: "center"
   },
   tooltip: {
    background: theme.palette.common?.white,
    border: "none",
    borderRadius: 3,
    boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
    color: theme.common?.grey[700],
  },
  popper: {
    marginTop: -theme.spacing(2),
  },
}));
