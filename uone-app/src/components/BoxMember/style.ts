import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    backgroundImage: (props: { background: string }) =>
      `url(${props.background})`,
    backgroundRepeat: "no-repeat",
    backgroundSize: "cover",
    borderRadius: "1.5vw",
    position: "relative",
    overflow: "hidden",
  },
}));

export default Style;
