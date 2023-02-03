import { makeStyles } from "@material-ui/core/styles";
import { StyledAvatarProps } from "./type";

const Style = makeStyles((theme) => ({
  wrapper: {
    padding: 2,
    border: (props: StyledAvatarProps) => `solid 2px ${props.borderOuterColor}`,
    borderRadius: "50%",
    backgroundColor: (props: StyledAvatarProps) => props.borderInnerColor,
    width: "55px",
    height: "55px",
    transform: (props: StyledAvatarProps) => `scale(${props.size})`,
  },
  avatar: {
    width: "100%",
    height: "100%",
    borderRadius: "50%",
    backgroundColor: (props: StyledAvatarProps) => props.background,
  },
}));

export default Style;
