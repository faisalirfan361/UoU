import {
  createContext,
  FC,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import useWebSocket from "react-use-websocket";
import { useRecoilValue } from "recoil";

import { userAtom } from "state";
import config from "config";
import { ImageApprovalMessage } from "components/NotificationsDrawer/types";
import moment from "moment";

interface WebSocketMessageType {
  userApprovals: any[];
  teamApprovals: any[];
  notifications: any[];
  lastRead: string;
}

interface NotificationsType {
  count: number;
  notifications: WebSocketMessageType;
  markNotificationAsRead(
    type: NotificationMessagesType,
    message: ImageApprovalMessage
  ): void;
  markAllNotificationAsRead(type: NotificationMessagesType): void;
  status: boolean;
}

export type NotificationMessagesType =
  | "notifications"
  | "userApprovals"
  | "teamApprovals";

const NotificationsContext = createContext<NotificationsType | undefined>(
  undefined
);

const parseNotification = (notification: any) => {
  return {
    ...notification,
    source: {
      ...notification.source,
      event: {
        ...notification.source.event,
        notificationData: JSON.parse(
          notification.source.event.notificationData
        ),
      },
    },
  };
};

function get48HoursToDate(objDate: Date, intHours: number) {
  var numberOfMlSeconds = objDate.getTime();
  var addMlSeconds = intHours * 60 * 60 * 1000;
  var newDateObj = new Date(numberOfMlSeconds - addMlSeconds);
  return newDateObj;
}

export const NotificationsProvider: FC = ({ children }) => {
  const { userId, jwtToken } = useRecoilValue(userAtom);
  const [notifications, setNotifications] = useState<WebSocketMessageType>({
    notifications: [],
    userApprovals: [],
    teamApprovals: [],
    lastRead: `2022-01-01T14:06:16.681Z`,
  });
  const url = config.webSocket + "?Authorizer=" + jwtToken;
  const { readyState } = useWebSocket(url, {
    shouldReconnect: () => true,
    onMessage: (event) => {
      const message = JSON.parse(event.data);
      const lastRead = message.lastRead
        ? message.lastRead
        : `2022-01-01T14:06:16.681Z`;
      const twoDaysBeforeDate = get48HoursToDate(new Date(), 48);
      if (message) {
        let updatedState = {};
        for (const key in message) {
          if (Array.isArray(message[key])) {
            updatedState = {
              ...updatedState,
              [key]: message[key].filter((notification: any) =>
                moment(twoDaysBeforeDate).isBefore(moment(notification.time))
              ),
              lastRead: lastRead,
            };
          } else if (key === "notifications") {
            if (message[key].data) {
              updatedState = {
                ...updatedState,
                notifications: message[key].data.map((notification: any) =>
                  parseNotification(notification)
                ),
                lastRead: lastRead,
              };
            }
          }
        }
        setNotifications((state) => {
          return { ...state, ...updatedState };
        });
      }
    },
  });

  const markNotificationAsRead = useCallback(
    (type: NotificationMessagesType, message: ImageApprovalMessage) => {
      setNotifications((state: WebSocketMessageType) => ({
        ...state,
        [type]: state[type].filter(
          (item: ImageApprovalMessage) => item.uuid !== message.uuid
        ),
      }));
    },
    [setNotifications]
  );

  const markAllNotificationAsRead = useCallback(
    (type: NotificationMessagesType) => {
      setNotifications((state: WebSocketMessageType) => ({
        ...state,
        lastRead: new Date().toISOString(),
      }));
    },
    [setNotifications]
  );

  const count = useMemo(() => {
    return Object.values(notifications).reduce(
      (count, item: any[]) =>
        notifications.notifications.filter((notification: any) =>
          moment(notifications.lastRead).isBefore(moment(notification.time))
        ).length,
      0
    );
  }, [notifications]);

  return (
    <NotificationsContext.Provider
      value={{
        count,
        notifications,
        markNotificationAsRead,
        markAllNotificationAsRead,
        status: Boolean(readyState),
      }}
    >
      {children}
    </NotificationsContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationsContext);

  if (context === undefined) {
    throw new Error(
      "useNotifications must be used inside a NotificationsProvider"
    );
  }

  return context;
};
