import { makeStyles } from "@material-ui/core/styles";

const Style = (props: { statusColor?: string }) =>
  makeStyles((theme) => ({
    root: {
      padding: 0,
      boxShadow: "0px 1px 10px rgba(0, 0, 0, 0.15)",
      borderRadius: 4,
    },
    cardContent: {},
    cardContentDivider: {},
    mainAvatar: {
      borderColor: `${
        props.statusColor ? props.statusColor : theme.palette.grey[900]
      }`,
    },
    userAvatar: {
      borderColor: `${
        props.statusColor ? props.statusColor : theme.palette.grey[900]
        }`,
    },
  }));

export default Style;
