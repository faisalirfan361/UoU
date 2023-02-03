import { useCallback } from "react";
import {
  Divider,
  Drawer,
  Grid,
  IconButton,
  List,
  Typography,
  Box,
} from "@material-ui/core";
import { useRecoilState } from "recoil";
import PerfectScrollbar from "react-perfect-scrollbar";
import { FaRegPaperPlane, FaTimes } from "react-icons/fa";

import { appAtom } from "state";
import useNotificationDrawerStyles from "./styles";
import StyledNotificationDrawer from "./StyledNotificationDrawer";
import NotificationItemSkeleton from "./NotificationSkeleton";
import { ImageApprovalMessage } from "./types";
import { useNotifications } from "context/NotificationsContext";
import TeamImageApprovalRequest from "./components/TeamImageApprovalRequest";
import UserImageApprovalRequest from "./components/UserImageApprovalRequest";
import NotificationsList from "./NotificationsList";

export default function NotificationsDrawer() {
  const notificationDrawerStyles = useNotificationDrawerStyles();
  const [appAtomState, setAppAtomState] = useRecoilState(appAtom);
  const {
    count,
    notifications,
    status: notificationsConnectionStatus,
  } = useNotifications();

  const toggleNotificationsDrawer = useCallback(() => {
    setAppAtomState((state) => ({
      ...state,
      showNotificationsDrawer: !state.showNotificationsDrawer,
    }));
  }, [appAtomState, setAppAtomState]);
  return (
    <StyledNotificationDrawer
      anchor="right"
      open={appAtomState.showNotificationsDrawer}
      onClose={toggleNotificationsDrawer}
      elevation={3}
    >
      <div className={notificationDrawerStyles.drawerHeader}>
        <Typography
          variant="h4"
          className={notificationDrawerStyles.drawerHeaderTitle}
        >
          <Box
            component="span"
            className={notificationDrawerStyles.drawerHeaderIcon}
          >
            <FaRegPaperPlane />
          </Box>
          Messages
        </Typography>
        <IconButton
          onClick={toggleNotificationsDrawer}
          size="small"
          className={notificationDrawerStyles.drawerCloseButton}
        >
          <FaTimes />
        </IconButton>
      </div>
      <Divider />
      {!notificationsConnectionStatus && <NotificationItemSkeleton />}
      {notificationsConnectionStatus &&
        notifications.notifications.length === 0 && (
          <Grid container alignItems="center" justify="center">
            <Grid item style={{ padding: "16px" }}>
              <Typography variant="subtitle1">
                No pending notifications.
              </Typography>
            </Grid>
          </Grid>
        )}
      {notificationsConnectionStatus && notifications.notifications.length > 0 && (
        <PerfectScrollbar>
          <Box className={notificationDrawerStyles.drawerNotificationsList}>
            <Typography variant="subtitle2">NEW </Typography>

            <List>
              {notifications.teamApprovals.map(
                (message: ImageApprovalMessage) => (
                  <TeamImageApprovalRequest
                    type="teamApprovals"
                    message={message}
                    key={message.uuid}
                  />
                )
              )}
              {notifications.userApprovals.map(
                (message: ImageApprovalMessage) => (
                  <UserImageApprovalRequest
                    type="userApprovals"
                    message={message}
                    key={message.uuid}
                  />
                )
              )}

              <NotificationsList notifications={notifications.notifications} />
            </List>
          </Box>
        </PerfectScrollbar>
      )}
    </StyledNotificationDrawer>
  );
}
