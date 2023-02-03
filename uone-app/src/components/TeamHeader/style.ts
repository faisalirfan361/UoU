import { makeStyles } from '@material-ui/core/styles';

const Styles = makeStyles((theme) => {
  return {
    container: {
      padding: "10px 0px"
    },
    feedItemCover: {
      width: '90%',
      display: 'flex',
      backgroundSize: 'cover !important',
      backgroundRepeat: 'no-repeat !important',
      height: 150,
      padding: 10,
      position: 'relative'
    },
    feedItemAvatarCenterBottom: {
      position: 'absolute',
      bottom: '-60%',
      left: '2%',
      transform: 'translate(0%, -50%)',
    },
  }
});

export default Styles;
