import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    marginBottom: theme.spacing(3),
  },
  postTitle:{
    textAlign: "left",
    fontSize:11,
    fontWeight:500,
  },
  postDate:{
    textAlign: "right",
    fontSize: 12,
    fontWeight:500,
  },
  thumbnailImg:{
    height:15,
    width: 15,
    marginRight:5,
    transform: 'translate(0px, 3px)',
  }
}));

export default Style;
