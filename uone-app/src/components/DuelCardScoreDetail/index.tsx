import React from "react";
import Grid from "@material-ui/core/Grid";
import DuelScoreDetailProps from "./types";
import Typography from "@material-ui/core/Typography";
import useDuelScoreDetailStyle from "./style";
import StyledAvatar from "components/StyledAvatar";

const DuelCardScoreDetail: React.FC<DuelScoreDetailProps> = ({
  status,
  user,
  opponent,
  userOneProfileImgSrc,
  userTwoProfileImgSrc,
}) => {
  const classes = useDuelScoreDetailStyle();
  const bgColor = status === 0 ? "#FFF" : status === 3 ? "#2FB0D9" : "#F5F7F7";
  const nameTextColor = status === 3 ? "#FFFFFF" : "#2FB0D9";
  const textColor = status === 3 ? "#FFFFFF" : "#171717";

  return (
    <>
      <Grid container spacing={0} className={classes.agentContainer}>
        <Grid item xs={12} className={classes.agentRowOne}>
          <Grid item xs={2}>
            <Typography className={classes.agentListHeading}>RANK</Typography>
          </Grid>
          <Grid item xs={7}>
            <Typography className={classes.agentListHeading}>
              AGENT NAME
            </Typography>
          </Grid>
          <Grid item xs={3}>
            <Typography className={classes.agentListHeading}>SCORE</Typography>
          </Grid>
        </Grid>
        <Grid
          item
          xs={12}
          className={classes.agentRowTwo}
          style={{
            backgroundColor: bgColor,
          }}
        >
          <Grid item xs={2}>
            <Typography
              className={classes.agentListText}
              style={{
                color: textColor,
              }}
            >
              1
            </Typography>
          </Grid>
          <Grid item xs={7} className={classes.agentAvatarContainer}>
            <StyledAvatar size={0.7} src={userOneProfileImgSrc} />
            <Typography
              className={classes.agentListName}
              style={{
                color: nameTextColor,
              }}
            >
              {user.firstName} {user.lastName}
            </Typography>
          </Grid>
          <Grid item xs={3}>
            <Typography
              className={classes.agentListText}
              style={{
                color: textColor,
              }}
            >
              {parseFloat(user.score?.toFixed(2))}
            </Typography>
          </Grid>
        </Grid>
        <Grid item xs={12} className={classes.agentRowThree}>
          <Grid item xs={2}>
            <Typography className={classes.agentListText}>2</Typography>
          </Grid>
          <Grid item xs={7} className={classes.agentAvatarContainer}>
            <StyledAvatar size={0.7} src={userTwoProfileImgSrc} />
            <Typography className={classes.agentListName}>
              {opponent.firstName} {opponent.lastName}
            </Typography>
          </Grid>
          <Grid item xs={3}>
            <Typography className={classes.agentListText}>
              {parseFloat(opponent.score?.toFixed(2))}
            </Typography>
          </Grid>
        </Grid>
      </Grid>
    </>
  );
};

export default DuelCardScoreDetail;
