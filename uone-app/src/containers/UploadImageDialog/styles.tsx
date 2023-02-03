import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  paper: {
    padding: 0,
    [theme.breakpoints.up("sm")]: {
      width: "600px",
    },
  },
}));

export const useUploadImageStyles = makeStyles((theme) => ({
  uploadImageDialogHeader: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    padding: "0 1em",
  },
}));
