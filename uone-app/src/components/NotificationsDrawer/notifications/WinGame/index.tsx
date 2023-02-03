import { FC } from "react";
import {
  ListItem,
  ListItemText,
  Typography,
  ListItemAvatar,
} from "@material-ui/core";

import StyledAvatar from "components/StyledAvatar";
import AgentWinGameStyle from "./style";
import AgentWinGameProps from "./types";

const WinGameNotification: FC<AgentWinGameProps> = ({ notification }) => {
  const agentWinGameStyle = AgentWinGameStyle();

  const notificationData = notification.source.event.notificationData;
  const author = notificationData.author;
  const authorName = `${author.firstName} ${author.lastName}`;
  const game = notificationData.metadata.game;

  return (
    <ListItem alignItems="center" divider>
      <ListItemAvatar>
        <StyledAvatar alt={authorName} src={author.profileImg} />
      </ListItemAvatar>
      <ListItemText
        className={agentWinGameStyle.listItemText}
        primary={
          <Typography>
            {`${authorName} just win a game!: ${game.title}`}
          </Typography>
        }
      />
    </ListItem>
  );
};

export default WinGameNotification;
