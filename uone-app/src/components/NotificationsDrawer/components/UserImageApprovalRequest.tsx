import {
  ListItem,
  ListItemText,
  Typography,
  ListItemIcon,
  Avatar,
  ListItemAvatar,
} from "@material-ui/core";
import { FaRegImage } from "react-icons/fa";

import useNotificationDrawerStyles from "../styles";
import { ImageApprovalMessage } from "../types";
import { NotificationMessagesType } from "context/NotificationsContext";
import StyledAvatar from "components/StyledAvatar";

export default function UserImageApprovalRequest({
  message,
  type,
}: {
  message: ImageApprovalMessage;
  type: NotificationMessagesType;
}) {
  const notificationDrawerStyles = useNotificationDrawerStyles();

  return (
    <ListItem alignItems="center" divider>
      {message.imageUrl ? (
        <ListItemAvatar>
          <StyledAvatar alt={message.userName} src={message.imageUrl} />
        </ListItemAvatar>
      ) : (
        <ListItemIcon className={notificationDrawerStyles.listItemIcon}>
          <FaRegImage size={32} />
        </ListItemIcon>
      )}
      <ListItemText
        primary={
          <Typography variant="body2">
            <Typography component="span" variant="subtitle2">
              You
            </Typography>{" "}
            requested {message.imageType === "PROFILE" ? "avatar" : "cover"}{" "}
            photo approval.
          </Typography>
        }
      />
    </ListItem>
  );
}
