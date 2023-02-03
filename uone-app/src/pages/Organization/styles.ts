import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  tabs: {
    "& .MuiTab-wrapper": {
      color: theme.palette.text.primary,
      textTransform: "initial",
    },
    "& .Mui-selected": {
      fontWeight: "bold",
    },
  },
  tabsContainer: {
    borderBottom: `1px solid ${theme.palette.text.secondary}`,
  },
}));

export default Style;
