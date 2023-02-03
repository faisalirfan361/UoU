import react from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { levelTableStyles } from "./style";
import { levelTableInfoConst } from "./constants";

const LevelInfoTable = () => {
  const classes = levelTableStyles();
  const bgColorWhite = "#FFFFFF";
  const bgColorGrey = "#EEEEEE";
  return (
    <>
      <Grid container spacing={0} className={classes.levelTableContainer}>
        <Grid item xs={12} className={classes.tableHead}>
          <Grid item xs={1}>
            <Typography className={classes.levelTableListHeading}>
              Levels
            </Typography>
          </Grid>
          <Grid item xs={3}>
            <Typography className={classes.levelTableListHeading}>
              Behavior Requirement
            </Typography>
          </Grid>
          <Grid item xs={2}>
            <Typography className={classes.levelTableListHeading}>
              Points Requirement
            </Typography>
          </Grid>
          <Grid item xs={2}>
            <Typography className={classes.levelTableListHeading}>
              Badge Earned
            </Typography>
          </Grid>
          <Grid item xs={2}>
            <Typography className={classes.levelTableListHeading}>
              Points Earned
            </Typography>
          </Grid>
          <Grid item xs={2}>
            <Typography className={classes.levelTableListHeading}>
              Customization Unlocked
            </Typography>
          </Grid>
        </Grid>
        {levelTableInfoConst.map((item, index) => (
          <Grid
            item
            xs={12}
            className={classes.levelTableRowTwo}
            style={{
              backgroundColor: index % 2 == 0 ? bgColorGrey : bgColorWhite,
            }}
          >
            <Grid item xs={1}>
              <Typography className={classes.singleListName}>
                {index}
              </Typography>
            </Grid>
            <Grid item xs={3} className={classes.levelTableAvatarContainer}>
              <Typography className={classes.singleListName}>
                {item.behaviorReq}
              </Typography>
            </Grid>
            <Grid item xs={2} className={classes.levelTableAvatarContainer}>
              <Typography className={classes.singleListName}>
                {item.ptsReq}
              </Typography>
            </Grid>
            <Grid item xs={2} className={classes.levelTableAvatarContainer}>
              {item.badgeEarned.image && <img src={item.badgeEarned.image} />}
              <Typography className={classes.levelTableListName}>
                {item.badgeEarned.txt}
              </Typography>
            </Grid>
            <Grid item xs={2} className={classes.levelTableAvatarContainer}>
              {item.ptsEarned.image && <img src={item.ptsEarned.image} />}
              <Typography className={classes.levelTableListName}>
                {item.ptsEarned.txt}
              </Typography>
            </Grid>
            <Grid item xs={2} className={classes.levelTableAvatarContainer}>
              {item.customUnlocked.image && (
                <img src={item.customUnlocked.image} />
              )}
              <Typography className={classes.levelTableListName}>
                {item.customUnlocked.txt}
              </Typography>
            </Grid>
          </Grid>
        ))}
      </Grid>
    </>
  );
};

export default LevelInfoTable;
