import { makeStyles } from "@material-ui/core/styles";
import clsx from "clsx";

const Style = makeStyles((theme) => ({
  root: {

  },
  likeBtn:{
    position:'relative',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    "& span":{
      fontSize:13,
      paddingLeft: 4,
      paddingRight:0,
      fontWeight: 500,
      color: '#2FB0D9',
      position:'relative',
      top: -6
    },
    "& svg":{
      fontSize:22,
      color: '#2FB0D9',
      position: 'relative',
      top: -1
    },
    "&:before":{
      content: '""',
      backgroundColor: "#2fb0d9",
      borderRadius: "50%",
      display: "block",
      position: "absolute",
      top: -5,
      right: 0,
      bottom: 0,
      left: 0,
      transform: "scale(0.001, 0.001)",
    },
    "&:focus": {
      outline: 0,
      "&:before": {
        animation: "$animEffectRadomir 0.8s ease-out",
      }
    }
  },
  "@keyframes animEffectRadomir":{
      "50%": {
        transform: 'scale(1.5, 1.5)',
        opacity: 0,
      },
      "99%": {
        transform: 'scale(0.001, 0.001)',
        opacity: 0,
      },
      "100%": {
        transform: 'scale(0.001, 0.001)',
        opacity: 1,
      }
  }

}));

export default Style;
