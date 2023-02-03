import { FC } from "react";
import {
  ListItem,
  ListItemText,
  Typography,
  ListItemAvatar,
} from "@material-ui/core";

import StyledAvatar from "components/StyledAvatar";
import PostLikeNotificationStyle from "./style";
import PostLikeNotificationProps from "./types";

const PostLikeNotification: FC<PostLikeNotificationProps> = ({
  notification,
}) => {
  const postLikeNotificationStyle = PostLikeNotificationStyle();

  const notificationData = notification.source.event.notificationData;
  const author = notificationData.author;
  const authorName = `${author.firstName} ${author.lastName}`;
  const challenge = notificationData.metadata.challenge;

  return (
    <ListItem alignItems="center" divider>
      <ListItemAvatar>
        <StyledAvatar alt={authorName} src={author.profileImg} />
      </ListItemAvatar>
      <ListItemText
        className={postLikeNotificationStyle.listItemText}
        primary={
          <Typography>
            {`${authorName} liked the challenge: ${challenge.title}`}
          </Typography>
        }
      />
    </ListItem>
  );
};

export default PostLikeNotification;
