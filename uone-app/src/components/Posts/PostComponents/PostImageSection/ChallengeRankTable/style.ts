import { makeStyles } from "@material-ui/core/styles";
import { common } from "@material-ui/core/colors";

const Style = makeStyles((theme) => ({
  root: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
    position: "relative",
  },
  itemContainer: {
    width: "100%",
  },
  table: {
    "& .MuiTableCell-stickyHeader": {
      backgroundColor: common.white,
      borderTop: `1px solid ${theme.common.grey[200]}`,
    },
    "& .MuiTableRow-head": {
      height: "unset",
    },
    "& tr": {
      height: 68,
    },
    "& th": {
      fontSize: 11,
      fontWeight: 500,
    },
    "& td": {
      fontSize: 14,
      fontWeight: 500,
      padding: "10px 15px",
    },
  },
  tableContainer: {
    maxHeight: 400,
  },
}));

export default Style;
