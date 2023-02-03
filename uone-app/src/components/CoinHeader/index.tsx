import React from "react";
import Grid from "@material-ui/core/Grid";

import IProps from "./types";
import useStyle from "./style";
import Avatar from "components/Avatar";
import FlagIcon from "@material-ui/icons/Flag";
import StarIcon from "@material-ui/icons/Star";
import CoinsIcon from "@material-ui/icons/MonetizationOn";
import Typography from "@material-ui/core/Typography";
import StyledAvatar from "components/StyledAvatar";

const CoinHeaderComponent: React.FC<IProps> = ({
  imageUrl,
  level,
  pointsToLevelUp,
  challengesWon,
  points,
  coins,
}) => {
  const styles = useStyle();

  return (
    <>
      <Grid container direction="row" className={styles.container}>
        <Grid item xs={5} className={`${styles.flexStart} ${styles.bordered}`}>
          <StyledAvatar src={imageUrl} />
          {/* <LevelIcon /> */}
          {/* <div>
            <Typography
              className={`${styles.text} ${styles.boldText}`}
              variant="subtitle2"
              align="right"
            >
              Level {level?.levelNumber} - {level?.name}
            </Typography>
            <Typography
              className={styles.text}
              variant="subtitle2"
              align="right"
            >
              {pointsToLevelUp} more points to level up
            </Typography>
          </div> */}
        </Grid>
        <Grid item xs={2} className={`${styles.flexStart} ${styles.bordered}`}>
          <FlagIcon />
          <div>
            <Typography
              className={`${styles.text} ${styles.boldText}`}
              variant="subtitle2"
              align="right"
            >
              {challengesWon}
            </Typography>
            <Typography
              className={styles.text}
              variant="subtitle2"
              align="right"
            >
              Wins
            </Typography>
          </div>
        </Grid>
        <Grid item xs={2} className={`${styles.flexStart} ${styles.bordered}`}>
          <StarIcon />
          <div>
            <Typography
              className={`${styles.text} ${styles.boldText}`}
              variant="subtitle2"
              align="right"
            >
              {points}
            </Typography>
            <Typography
              className={styles.text}
              variant="subtitle2"
              align="right"
            >
              Points
            </Typography>
          </div>
        </Grid>
        <Grid item xs={3} className={`${styles.flexStart}`}>
          <CoinsIcon />
          <div>
            <Typography
              className={`${styles.text} ${styles.boldText}`}
              variant="subtitle2"
              align="right"
            >
              {coins}
            </Typography>
            <Typography
              className={styles.text}
              variant="subtitle2"
              align="right"
            >
              Coins
            </Typography>
          </div>
        </Grid>
      </Grid>
    </>
  );
};

export default CoinHeaderComponent;
