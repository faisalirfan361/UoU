import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    marginBottom: 30,
    position: 'relative',
  },
  imagesContainer:{
    height:155,
    padding:'0px',
    position:'relative',

  },
  agentDivCoverGeneral:{
    height:'100%',
    backgroundColor: 'grey',
    width:'100%',
    borderRadius:5
  },
  agentOneProfileImg:{
    height:90,
    width:90,
    position:'absolute',
    bottom: -20,
    right:'40%',
  },
  agentTwoProfileImg:{
    height:95,
    width:95,
    position: 'absolute',
    left:'20%',
    bottom:'5%',
  },
}));

export default Style;
