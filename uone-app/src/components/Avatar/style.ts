import { makeStyles } from '@material-ui/core/styles';

const Styles = (backgroundColor: string = '#252525', dimension: number = 54) => makeStyles((theme) => {
  return {
    avatar: {
      background: backgroundColor,
      width: dimension,
      height: dimension,
      borderRadius: "50%",
      border: `2px solid white`,
      boxShadow: `0 0 0 2px ${backgroundColor}`
    }
  }
});

export default Styles;
