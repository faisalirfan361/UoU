import {
  ListItem,
  ListItemText,
  Typography,
  ListItemIcon,
  Grid,
} from "@material-ui/core";
import useNotificationDrawerStyles from "./styles";
import { Skeleton } from "@material-ui/lab";

export default function NotificationItemSkeleton() {
  const notificationDrawerStyles = useNotificationDrawerStyles();
  return (
    <ListItem alignItems="flex-start" divider>
      <ListItemIcon className={notificationDrawerStyles.listItemIcon}>
        <Skeleton animation="wave" variant="circle" height={32} width={32} />
      </ListItemIcon>
      <ListItemText
        primary={
          <Typography component="span">
            <Skeleton
              variant="rect"
              animation="wave"
              height={20}
              width="80%"
              className={notificationDrawerStyles.skeletonWithMargin}
            />
          </Typography>
        }
        secondary={
          <Grid component="span" container spacing={1}>
            <Grid component="span" item>
              <Skeleton
                animation="wave"
                variant="rect"
                height={20}
                width={68}
              />
            </Grid>
            <Grid component="span" item>
              <Skeleton
                animation="wave"
                variant="rect"
                height={20}
                width={68}
              />
            </Grid>
          </Grid>
        }
      />
    </ListItem>
  );
}
