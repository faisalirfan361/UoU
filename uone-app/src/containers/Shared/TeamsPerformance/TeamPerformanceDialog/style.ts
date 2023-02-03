import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles(() => {
  return {
    actions: {
      height: 40,
      paddingTop: 0,
    },
    closeButton: {
      position: "absolute",
      right: "11px",
    },
  };
});

export default Styles;
