import { makeStyles } from '@material-ui/core/styles';

const Styles = makeStyles((theme) => {
  return {
    root:{
      boxShadow: theme.shadows[2],
      "& .MuiTable-root":{
        width:"100%",
        maxWidth:"700px",
      }
    },
    userAvatar:{
      width: 40,
      height:40,
      marginRight: theme.spacing(3),
    },
    performanceIcon:{
      fontSize:"31px",
      position: "relative",
      top:"5px",
      display: "flex",
      justifyContent: "center",
    },
    agentName:{
      fontSize:14,
      fontWeight:500,
      color: theme.common.uoneLightBlue[500],
    },
    number:{
      fontSize:18,
      fontWeight:500,
    },
    points:{
      fontSize:14,
      fontWeight:500,
    },
    ownScore:{
      "& td":{
        backgroundColor: theme.common.uoneLightBlue[500],
        color: `${theme.palette.common.white}`,
        "& p":{
          color: `${theme.palette.common.white}`,
        }
      },
    },
  }
});

export default Styles;
