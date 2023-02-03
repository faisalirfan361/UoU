import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    paddingTop:theme.spacing(1),
    paddingBottom:theme.spacing(2),
  },
  imgGrid:{
    maxWidth: 40,
    flexBasis: 40,
  },
  textAreaGrid:{
    maxWidth: 'calc(100% - 40px)',
    flexBasis: 'calc(100% - 40px)',
    fallbacks: [
      { maxWidth: '-moz-calc(100% - 40px)' },
      { maxWidth: '-webkit-calc(100% - 40px)' },
      { maxWidth: '-o-calc(100% - 40px)' },
      { flexBasis: '-moz-calc(100% - 40px)' },
      { flexBasis: '-webkit-calc(100% - 40px)' },
      { flexBasis: '-o-calc(100% - 40px)' }
    ],
  },
  textAreaContainer:{
    paddingLeft:10,
    position:'relative',
  },
  textArea:{
    width:'100%',
    minHeight:42,
    resize:'none',
    border:'1px solid #3B3B3B',
    opacity: 0.4,
    boxSizing: 'border-box',
    borderRadius: 3,
    padding: '5px 50px 5px 5px',
    fontSize:18
  },
  profileImg:{
    width:40,
    height: 40,
  },
  iconSendContainer:{
    cursor: 'pointer',
    position:'absolute',
    top:7,
    right:10,
    width:28,
    height:28,
    textAlign:'right',
    "& svg":{
      width:'100%',
      height:'100%',
      color:'#2FB0D9'
    }
  },
  iconSendContainerDisable:{
    position:'absolute',
    top:7,
    right:10,
    width:28,
    height:28,
    textAlign:'right',
    "& svg":{
      width:'100%',
      height:'100%',
      color:'#2FB0D9'
    },
    opacity: "0.3",
    pointerEvents: "none"
  }

}));

export default Style;
