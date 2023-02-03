import {
  Grid,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from "@material-ui/core";

import {
  useMainHeaderStyles,
  useMainHeaderCardContentStyles,
  usePerformanceListItemTextStyles,
} from "../styles";

import Typography from "@material-ui/core/Typography";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import config from "config";
import { FaCoins, FaFontAwesomeFlag, FaStar } from "react-icons/fa";
import AvatarComponent from "components/Avatar";

export default function PerformanceBar({ showAvatar }: any) {
  const {
    pointsBalance,
    pointsCumulative,
    avatarImages,
    departmentName,
    uoneData,
  } = useRecoilValue(userAtom);
  const cardStyles = useMainHeaderCardContentStyles({ showAvatar });
  const styles = useMainHeaderStyles({});
  const listItemTextStyles = usePerformanceListItemTextStyles();

  const avatarSrc = `${config.targetBucketUrl}${avatarImages?.keys.medium}`;
  const level = 1;
  const points = Number.isInteger(pointsCumulative) ? pointsCumulative : 0;
  const coins = Number.isInteger(pointsBalance) ? pointsBalance : 0;
  const wins = Number.isInteger(uoneData?.challengeWonCount)
    ? uoneData?.challengeWonCount
    : 0;

  return (
    <CardContent className={cardStyles.content}>
      <Grid
        container
        direction="row"
        alignItems="center"
        justify="space-between"
      >
        <Grid item>
          <Grid container alignItems="center" spacing={2}>
            {showAvatar && (
              <Grid item>
                <AvatarComponent src={avatarSrc} />
              </Grid>
            )}
            <Grid item>
              <Typography variant="subtitle2" className={styles.teamLabel}>
                Team
              </Typography>
              <Typography variant="subtitle1">{departmentName}</Typography>
            </Grid>
          </Grid>
        </Grid>
        <Grid item>
          <List className={styles.performanceList}>
            <ListItem className={styles.performanceListItem}>
              <ListItemText
                primary={`Level ${level}`}
                classes={listItemTextStyles}
              />
            </ListItem>
            <ListItem className={styles.performanceListItem}>
              <ListItemIcon className={styles.performanceListItemIcon}>
                <FaFontAwesomeFlag />
              </ListItemIcon>
              <ListItemText
                primary={wins}
                secondary="Wins"
                classes={listItemTextStyles}
              />
            </ListItem>
            <ListItem className={styles.performanceListItem}>
              <ListItemIcon className={styles.performanceListItemIcon}>
                <FaStar />
              </ListItemIcon>
              <ListItemText
                primary={points}
                secondary="Points"
                classes={listItemTextStyles}
              />
            </ListItem>
            <ListItem className={styles.performanceListItem}>
              <ListItemIcon className={styles.performanceListItemIcon}>
                <FaCoins />
              </ListItemIcon>
              <ListItemText
                primary={coins}
                secondary="Coins"
                classes={listItemTextStyles}
              />
            </ListItem>
          </List>
        </Grid>
      </Grid>
    </CardContent>
  );
}
