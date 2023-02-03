import {
  ListItem,
  ListItemText,
  Typography,
  ListItemIcon,
  Button,
  Grid,
  Avatar,
  ListItemAvatar,
} from "@material-ui/core";
import { FaRegImage } from "react-icons/fa";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import config from "config";
import useNotificationDrawerStyles from "../styles";
import { ImageApprovalMessage } from "../types";
import {
  NotificationMessagesType,
  useNotifications,
} from "context/NotificationsContext";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import StyledAvatar from "components/StyledAvatar";
import { useState } from "react";

export default function TeamImageApprovalRequest({
  message,
  type,
}: {
  message: ImageApprovalMessage;
  type: NotificationMessagesType;
}) {
  const [buttonStatus, setButtonStatus] = useState(false);
  const { departmentId } = useRecoilValue(userAtom);
  const { enqueueSnackbar } = useSnackbar();
  const notificationDrawerStyles = useNotificationDrawerStyles();
  const { markNotificationAsRead } = useNotifications();
  const imageApproveUrl = `/user/users/image/approve/${message.userId}`;
  const basePayload = {
    uuid: message.uuid,
    type: message.imageType,
    clientId: message.clientId,
    departmentId,
  };

  const handleApprove = async () => {
    setButtonStatus(true);
    try {
      await API.post(config.apiGateway.NAME, imageApproveUrl, {
        body: {
          ...basePayload,
          approved: 1,
        },
      });
      markNotificationAsRead(type, message);
      enqueueSnackbar("Image approved successfully.", SUCCESS_TOAST_OPTIONS);
    } catch (error) {
      enqueueSnackbar("Failed to approve Image.", ERROR_TOAST_OPTIONS);
    }
    setButtonStatus(false);
  };

  const handleDeny = async () => {
    setButtonStatus(true);
    try {
      await API.post(config.apiGateway.NAME, imageApproveUrl, {
        body: {
          ...basePayload,
          approved: 0,
        },
      });
      markNotificationAsRead(type, message);
      enqueueSnackbar("Image was deny.", SUCCESS_TOAST_OPTIONS);
    } catch (error) {
      enqueueSnackbar("Failed to deny image.", ERROR_TOAST_OPTIONS);
    }
    setButtonStatus(false);
  };
  const download = () => {
    window.open(message.imageUrl, "_blank");
  };

  return (
    <ListItem alignItems="flex-start" divider>
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
              {message.userName}{" "}
            </Typography>
            requested {message.imageType === "PROFILE" ? "avatar" : "cover"}{" "}
            photo approval.
          </Typography>
        }
        secondary={
          <Grid component="span" container spacing={1}>
            <Grid component="span" item>
              <Button
                color="primary"
                size="small"
                onClick={handleApprove}
                disabled={buttonStatus}
              >
                APPROVE
              </Button>
            </Grid>
            <Grid component="span" item>
              <Button
                color="secondary"
                size="small"
                onClick={handleDeny}
                disabled={buttonStatus}
              >
                DENY
              </Button>
            </Grid>
            <Grid component="span" item>
              <Button color="secondary" size="small" onClick={download}>
                PREVIEW
              </Button>
            </Grid>
          </Grid>
        }
      />
    </ListItem>
  );
}
