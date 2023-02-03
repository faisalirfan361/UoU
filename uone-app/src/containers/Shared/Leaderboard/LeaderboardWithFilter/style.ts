import { makeStyles } from '@material-ui/core/styles';


const Styles = makeStyles((theme) => {
  return {
    root:{
      paddingTop:  theme.spacing(1),
      paddingBottom: theme.spacing(1),

    },
    departmentsSelector:{
      marginBottom: theme.spacing(3),
    },
    leaderBoard:{
      display: "flex",
      justifyContent: "center"
    },
  }
});

export default Styles;
