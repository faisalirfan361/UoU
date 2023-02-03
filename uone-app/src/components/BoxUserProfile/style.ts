import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    "&:before": {
      content: '""',
      position: "absolute",
      left: 0,
      width: 0,
      height: 0,
      borderStyle: "solid",
      borderWidth: "4.5rem 4.5rem 0 0",
      borderColor: (styleProps: any) =>
        `${styleProps.statusColor} transparent transparent transparent`,
    },
  },
  card: {
    background: "transparent",
    color: (styleProps: any) => styleProps.statusColor,
  },
  cardContent: {
    "&:last-child": {
      paddingBottom: theme.spacing(1) + 2,
    },
  },
  bullet: {
    display: "inline-block",
    margin: "0 2px",
    transform: "scale(0.8)",
  },
  title: {
    fontSize: 14,
  },
  pos: {
    marginBottom: 12,
  },
  avatar: {
    margin: "0 auto",
    width: "65%",
    height: "auto",
    border: "0.56vw solid rgb(255, 255, 255)",
    backgroundColor: "rgb(255, 255, 255)",
    marginBottom: theme.spacing(2),
  },
  badge: {
    textAlign: "center",
    float: "left",
    width: "15%",
    height: "15%",
    margin: theme.spacing(1) / 4,
    marginBottom: 0,
    border: "3px solid rgb(255, 255, 255)",
    backgroundColor: "rgb(255, 255, 255)",
  },
  fullNameGrid: {
    padding: theme.spacing(1) - 2,
    backgroundColor: "#000",
  },
  fullNameTypography: {
    fontWeight: "bold",
  },
}));

export default Style;
