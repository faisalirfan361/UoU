import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    display: "flex",
    position: "relative",
    paddingTop: theme.spacing(2),
    paddingBottom: theme.spacing(4),
  },
  titles: {
    paddingLeft: theme.spacing(2),  
    "& p": {
      margin: 0,
    },
    "& .primaryText": {
      fontSize: 18,
      color: theme.common?.grey[900],
    },
    "& .secondaryText": {
      fontSize: 11,
      color: theme.common?.grey[500],
      overflow: "hidden",
      textOverflow: "ellipsis",
      lineClamp: 1
    },
    "& .MuiInputBase-input": {
      padding: 0
    }
  },
  status: {
    display: "inline-block",
    position: "absolute",
    bottom: theme.spacing(2),
    right: "0px",
    "& .statusBall": {
      width: 14,
      height: 14,
      borderRadius: "50%",
      display: "inline-block",
      position: "relative",
      top: 2,
      marginRight: 5,
    },
    "& .statusText": {
      size: 14,
      fontWeight: 500,
    },
  },
  secondaryTextfield: {
    fontSize: 11,
    color: theme.common?.grey[500],
    height: theme.spacing(2),
    marginLeft: theme.spacing(0.5),
  },
  boxContainer: {
    display: "flex",
    justifyItems: "center"
  },
  secondaryText: {
    fontSize: 11,
    color: theme.common?.grey[500],
    overflow: "hidden",
    textOverflow: "ellipsis",
    lineClamp: 1
  },
}));

export default Style;
