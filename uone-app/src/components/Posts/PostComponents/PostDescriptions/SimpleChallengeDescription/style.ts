import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    marginBottom: 15,
    paddingLeft: "1em",
    paddingRight: "1em",
  },
  iconSection:{

  },
  avatarStyle:{
    backgroundColor: 'white',
    margin: 'auto',
    height:55,
    width:55,
    border: '2px solid #944EDC',
    marginTop:-6
  },
  icon:{
    height:45,
    width:45,
    backgroundColor: '#F25BA4',
    padding:10,
    borderRadius: '50%'
  },
  descSection:{
    fontSize:14,
    fontWeight:500,

    '& .participants':{
      fontSize:14
    },
    '& .name':{
      color:'#2FB0D9'
    },
    '& .details':{
      fontSize:12,
      overflowWrap: "break-word",
    }
  }
}));

export default Style;
