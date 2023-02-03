import React, { FC } from "react";
import {
  ListItem,
  ListItemText,
  Typography,
  ListItemIcon,
  Avatar,
} from "@material-ui/core";
import InfoIcon from "@material-ui/icons/Info";

import NormalNotificationStyles from "./style";
import NormalNotificationProps from "./types";

const NormalNotification: FC<NormalNotificationProps> = ({ notification }) => {
  const normalNotificationStyles = NormalNotificationStyles();
  const title: string = notification.tille ? notification.tille : "";
  const message: string = notification.message;

  return (
    <ListItem alignItems="center" divider>
      <ListItemIcon className={normalNotificationStyles.listItemIcon}>
        <InfoIcon className={normalNotificationStyles.icon} />
      </ListItemIcon>

      <ListItemText
        className={normalNotificationStyles.listItemText}
        primary={<Typography>{title}</Typography>}
        secondary={<Typography>{message}</Typography>}
      />
    </ListItem>
  );
};

export default NormalNotification;
