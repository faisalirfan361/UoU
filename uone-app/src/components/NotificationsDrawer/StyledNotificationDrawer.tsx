import { Drawer } from "@material-ui/core";
import { withStyles } from "@material-ui/core";
import { createStyles } from "@material-ui/styles";

export default withStyles(() =>
  createStyles({
    paper: {
      minWidth: 250,
      maxWidth: 600,
      width: "100%",
    },
  })
)(Drawer);
