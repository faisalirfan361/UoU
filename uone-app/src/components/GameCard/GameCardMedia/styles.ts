import { makeStyles, darken } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  wrapper: {
    backgroundImage: `linear-gradient(90deg, #7B827E -1.49%, #2FB0D9 -1.48%, #286D84 51.58%, #2FB0D9 96.48%)`,
  },
  media: {
    backgroundImage: (props: { image: string }) => `url(${props.image})`,
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    height: 0,
    paddingTop: 140,
    width: "100%",
  },
}));
