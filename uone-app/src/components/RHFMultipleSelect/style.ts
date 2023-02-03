import { makeStyles } from "@material-ui/core/styles";

import { zIndex } from "../../constants";

export default makeStyles((theme) => ({
  select: {
    zIndex: zIndex.select - 1,
  },
}));
