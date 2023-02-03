import React from "react";
import Grid from "@material-ui/core/Grid";
import Interweave from "interweave";
import Typography from "@material-ui/core/Typography";
import { Tooltip, Avatar } from "@material-ui/core";

import IProps from "./types";
import useStyle from "./style";
import Button from "components/ButtonAction";

const CoinStoreItemComponent: React.FC<IProps> = ({
  imageUrl,
  title,
  description,
  points,
  canRedeem,
  reachedDailyPoints,
  onClick,
}) => {
  const styles = useStyle();
  const [showMaxDailyPointsReached, setShowMaxDailyPointsReached] =
    React.useState(false);

  const handleCloseMaxDailyPointsReached = () => {
    setShowMaxDailyPointsReached(false);
  };

  const handleOpenMaxDailyPointsReached = () => {
    setShowMaxDailyPointsReached(reachedDailyPoints || !canRedeem);
  };

  return (
    <>
      <Grid container direction="row" className={styles.container}>
        <Grid container direction="row" className={styles.row}>
          <Avatar
            src={imageUrl}
            variant="square"
            className={styles.coinStoreThumbnail}
          />
          <Typography
            className={styles.title}
            variant="subtitle2"
            align="right"
          >
            {title}
          </Typography>
        </Grid>
        <Grid container direction="row" className={styles.row}>
          <Typography className={styles.description} variant="caption">
            <Interweave content={description} />
          </Typography>
        </Grid>
        <Grid container direction="row" className={styles.row}>
          <Tooltip
            open={showMaxDailyPointsReached}
            onClose={handleCloseMaxDailyPointsReached}
            onOpen={handleOpenMaxDailyPointsReached}
            title="Max daily points reached"
          >
            <span>
              <Button handleOnClick={onClick} disabled={!canRedeem}>
                GET ITEM
              </Button>
            </span>
          </Tooltip>
          <Typography
            className={styles.points}
            variant="subtitle2"
            align="right"
          >
            {points}
            <sup className={styles.sup}>coins</sup>
          </Typography>
        </Grid>
      </Grid>
    </>
  );
};

export default CoinStoreItemComponent;
