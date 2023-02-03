import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => {
  return {
    adminActions: {
      display: "flex",
      justifyContent: "flex-end",
    },
    disclaimer: {
      fontSize: "7pt",
      maxWidth: "50%",
      margin: "20px auto 0px auto",
    },
    paginationBox: {
      display: "flex",
      justifyContent: "center",
    },
  };
});
