// @ts-nocheck

import { makeStyles } from "@material-ui/core/styles";

const Style =  makeStyles((theme) => ({
    root: {
      display: 'flex',
      marginLeft:-18,
      marginTop:4,
      "& img":{
        borderRadius: '50%',
      }
    },
    avatarPrimaryContainer:{
      padding:2,
      border: "solid 2px black",
      borderRadius: "50%",
      backgroundColor: "white",
      width: "57px",
      height: "57px",
      zIndex: 3,
    },
    avatar: {
      width: "100%",
      height: "100%",
      borderRadius: "50%",
      backgroundColor: "#252525",

    },
    avatarSecondaryContainer:{
      padding:2,
      border: "solid thin black",
      borderRadius: "50%",
      backgroundColor: "white",
      width: "40px",
      height: "40px",
      zIndex: 2,
      position: "relative",
      top:-4,
    },
    avatarLeftContainer:{
      left: 18
    },
    avatarRightContainer:{
      right: 18
    },
  }));

export default Style;
