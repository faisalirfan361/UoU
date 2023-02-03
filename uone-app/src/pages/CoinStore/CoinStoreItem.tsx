import {
  Box,
  Button,
  Card,
  CardContent,
  CardMedia,
  CardHeader,
  Tooltip,
  Typography,
} from "@material-ui/core";
import { useState } from "react";
import EditButton from "components/EditButton";
import coinStoreItemClasses from "./coinStoreItemStyles";
import { Can } from "context/Ability/Can";

export default function CoinStoreItem({ item, viewItem, editItem }: any) {
  const classes = coinStoreItemClasses();
  const [showMaxDailyPointsReached, setShowMaxDailyPointsReached] =
    useState(false);

  const handleCloseMaxDailyPointsReached = () => {
    setShowMaxDailyPointsReached(false);
  };

  const handleOpenMaxDailyPointsReached = () => {
    setShowMaxDailyPointsReached(item.reachedDailyPoints || !item.canRedeem);
  };

  return (
    <Card className={classes.coinStoreItem}>
      <CardHeader
        className={classes.cardHeader}
        avatar={
          <CardMedia
            component="img"
            className={classes.coinStoreItemThumbnail}
            image={item.imageUrl}
            title={item.title}
          />
        }
        action={
          !item.brand && (
            <Can I="edit" a="coin-store">
              <Box mb={1} component="div">
                <EditButton
                  disableRipple={false}
                  onClick={editItem}
                  className={classes.coinStoreButton}
                />
              </Box>
            </Can>
          )
        }
      />
      <CardContent className={classes.coinStoreItemContent}>
        <Typography component="h5" variant="h5">
          {item.title}
        </Typography>
        <Typography variant="body2" color="textSecondary">
          <span dangerouslySetInnerHTML={{ __html: item.description }}></span>
        </Typography>
      </CardContent>

      <div className={classes.coinStoreItemAction}>
        <Tooltip
          title="Max daily points reached"
          open={showMaxDailyPointsReached}
          onClose={handleCloseMaxDailyPointsReached}
          onOpen={handleOpenMaxDailyPointsReached}
        >
          <Button
            variant="contained"
            color="primary"
            disabled={!item.canRedeem}
            onClick={viewItem}
          >
            Redeem
          </Button>
        </Tooltip>
        <Box mt={1}>
          <Typography>{item.points} coins</Typography>
        </Box>
      </div>
    </Card>
  );
}
