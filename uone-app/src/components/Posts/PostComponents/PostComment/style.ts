import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    paddingTop:theme.spacing(4),
    paddingBottom:theme.spacing(4),
  },
  imgGrid:{
    maxWidth: 40,
    flexBasis: 40,
  },
  commentGrid:{
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
  profileImg:{
    width:40,
    height: 40,
  },
  commentTitle:{
    margin:0,
  },
  userName:{
    paddingTop:0,
    paddingLeft:10,
    color: '#2FB0D9',
    fontSize:14,
    fontWeight:500,
  },
  commentText:{
    margin: '-10px 0px 0px 10px',
    backgroundColor: '#F5F7F7',
    borderRadius: 3,
    padding:10,
    fontSize:14,
    fontWeight:400,
    color:'#3B3B3B',
  },
  date:{
    float:'right',
    fontSize:12,
    fontWeight:400,
    lineHeight:'18px',
  }
}));

export default Style;
