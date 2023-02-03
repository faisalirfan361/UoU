import { FC } from "react";
import {
  ListItem,
  ListItemText,
  Typography,
  ListItemAvatar,
} from "@material-ui/core";

import StyledAvatar from "components/StyledAvatar";
import PostCommentNotificationStyle from "./style";
import PostCommentNotificationProps from "./types";

const PostCommentNotification: FC<PostCommentNotificationProps> = ({
  notification,
}) => {
  const postCommentNotificationStyle = PostCommentNotificationStyle();

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
        className={postCommentNotificationStyle.listItemText}
        primary={
          <Typography>
            {`${authorName} made a comment on the challenge: ${challenge.title}`}
          </Typography>
        }
      />
    </ListItem>
  );
};

export default PostCommentNotification;
