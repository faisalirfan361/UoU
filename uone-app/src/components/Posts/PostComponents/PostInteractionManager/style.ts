import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    padding: '0px 15px 0px 15px',
  },
  container:{
    borderTop: "solid thin black",
    padding:'20px 10px 15px 10px'
  },
  commentsContainer:{
    paddingBottom: theme.spacing(2),
  },
  infoContainer:{
    color: '#252525b5'
  },
  actionsContainer:{
    color: '#2FB0D9'
  },
  flexWrapperInfo:{
    display:"flex"
  },
  flexWrapperActions:{
    display:"flex",
    justifyContent: "flex-end",
    cursor: 'pointer'
  },
  wrapperItemInfo:{
    paddingRight: 5,
    display: "flex",
    "& span":{
      fontSize:13,
      paddingLeft: 4,
      paddingRight:15,
      fontWeight: 500
    },
    "& svg":{
      fontSize:22
    },
    "& .count":{
      marginTop:1
    },
    "& .icon-comment":{
      marginTop:2
    },
    "& .icon-like":{
      marginTop:0
    }
  },
  wrapperItemActions:{
    paddingLeft: 25,
    display: "flex",
    "& span":{
      fontSize:13,
      paddingLeft: 4,
      paddingRight:0,
      fontWeight: 500
    },
    "& svg":{
      fontSize:22
    },
    "& .label":{
      marginTop:2
    },
    "& .icon-comment":{
      marginTop:2
    },
    "& .icon-like":{
      marginTop:0
    }
  }


}));

export default Style;
