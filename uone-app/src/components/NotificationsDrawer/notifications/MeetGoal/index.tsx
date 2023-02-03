import { FC } from "react";
import {
  ListItem,
  ListItemText,
  Typography,
  ListItemAvatar,
} from "@material-ui/core";

import StyledAvatar from "components/StyledAvatar";
import AgentMeetGoalStyle from "./style";
import AgentMeetGoalProps from "./types";

const MeetGoalNotification: FC<AgentMeetGoalProps> = ({ notification }) => {
  const agentMeetGoalStyle = AgentMeetGoalStyle();

  const notificationData = notification.source.event.notificationData;
  const author = notificationData.author;
  const authorName = `${author.firstName} ${author.lastName}`;
  const goal = notificationData.metadata.challenge;

  return (
    <ListItem alignItems="center" divider>
      <ListItemAvatar>
        <StyledAvatar alt={authorName} src={author.profileImg} />
      </ListItemAvatar>
      <ListItemText
        className={agentMeetGoalStyle.listItemText}
        primary={
          <Typography>
            {`${authorName} just met a goal!: ${goal.title}`}
          </Typography>
        }
      />
    </ListItem>
  );
};

export default MeetGoalNotification;
