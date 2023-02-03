import { FC, useEffect } from "react";
import { API } from "aws-amplify";
import { useRecoilValue } from "recoil";

import { userAtom } from "state";
import { NotificationsTypes } from "../../../constants";
import { Notification } from "../notifications/notificationsTypes";
import config from "../../../config";
import PostCommentNotification from "../notifications/PostComment";
import PostLikeNotification from "../notifications/PostLike";
import NormalNotification from "../notifications/NormalNotification";
import NotificationsListProps from "./types";

const getNotificationComponent = (notification: Notification): any => {
  return (
    <NormalNotification key={notification.id} notification={notification} />
  );
};

const NotificationList: FC<NotificationsListProps> = ({ notifications }) => {
  const { userId } = useRecoilValue(userAtom);

  if (!notifications || notifications.length === 0) {
    return null;
  }

  useEffect(() => {}, [notifications]);

  return (
    <>
      {notifications.map((notification: any) => {
        return getNotificationComponent(notification);
      })}
    </>
  );
};

export default NotificationList;
