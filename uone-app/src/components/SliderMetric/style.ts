import { makeStyles } from "@material-ui/core/styles";

const Styles = makeStyles((theme) => ({
    root: {
      color: (props: any) => `solid 2px ${props.statusColor}`,
      height: 14,
      padding: 0,
      margin: 0,
    },
    label: {
      textAlign: "right",
      padding: 0,
      margin: 0,
    },
    labelTypography: {
      fontStyle: "normal",
      fontWeight: 500,
      fontSize: 14,
      color: "#052C42",
      textAlign: "right",
      position: "relative",
      top: -4,
      cursor: "pointer"
    },
    inputLable: {
      "& .MuiInputBase-input": {
        fontStyle: "normal",
        fontWeight: 500,
        fontSize: 14,
        color: "#052C42",
        textAlign: "right",
        padding: 0
      }
    },

    slider: {
      padding: `${theme.spacing(0)}px , ${theme.spacing(1)}px`,
    },
    thumb: {
      display: "none",
    },
    active: {},
    valueLabel: {
      left: "calc(-50% - 6px)",
      color: "#F8AB3D",
    },
    track: {
      height: 10,
      borderRadius: 2,
      borderBottomLeftRadius: "none",
      borderTopLeftRadius: "none",
    },
    rail: {
      height: 10,
      borderRadius: 2,
      borderBottomLeftRadius: "none",
      borderTopLeftRadius: "none",
      backgroundColor: `${theme.common.grey[200]}`,
    },
    tooltip: {
      background: theme.palette.common.white,
      border: "none",
      borderRadius: 3,
      boxShadow: "0px 1px 8px rgba(0, 0, 0, 0.15)",
      color: theme.common.grey[700],
    },
    popper: {
      marginTop: -theme.spacing(4),
    },
    percentage: {
      height: 20,
      background: (props: any) => `linear-gradient(left, ${props.background} ${props.percentage}%,${theme.common.grey[200]} ${props.percentage}%)`,
      textAlign: "center",
      padding: "0px 20px",
      fontSize: 14,
      overflow: "hidden",
      textOverflow: "ellipsis",
      lineClamp: 1
    }
}));

export default Styles;