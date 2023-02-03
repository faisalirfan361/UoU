import { makeStyles } from "@material-ui/core/styles";

const Style = (props: { statusColor?: string }) =>
  makeStyles((theme) => ({
    root: {
      padding: 0,
      borderRadius: 4,
    },
    userAvatar: {
      borderColor: `${
        props.statusColor ? props.statusColor : theme.palette.grey[900]
      }`,
      width: 55,
      height: 55,
      position: "relative",
      top: -7,
      marginRight: 8,
    },
  }));

export default Style;
