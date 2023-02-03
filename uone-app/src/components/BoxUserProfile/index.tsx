import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";

import { AvatarComponent } from "components";

import IProps from "./types";
import useStyles from "./style";

const BoxUserProfile: FC<IProps> = ({ user }) => {
  const classes = useStyles({
    background: user.background,
    statusColor: user.statusColor,
  });

  if (!user) return null;

  return (
    <Box className={classes.root}>
      <Card className={classes.card}>
        <CardContent className={classes.cardContent}>
          <AvatarComponent src={user.avatar} className={classes.avatar} />
          <Grid container direction="row">
            <Grid item xs={12}>
              <Box
                display="flex"
                flexWrap="wrap"
                alignContent="center"
                justifyContent="center"
              >
                {user.badges?.map((badge: number, index: number) => {
                  let badgeSrc = require(`../../assets/img/badge/${badge}.png`);
                  return (
                    <AvatarComponent
                      key={index}
                      src={badgeSrc.default}
                      className={classes.badge}
                    />
                  );
                })}
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>
      <Grid container direction="row" className={classes.fullNameGrid}>
        <Grid item xs={12}>
          <Typography
            variant="body2"
            className={classes.fullNameTypography}
          >{`${user.lastName} ${user.firstName}`}</Typography>
        </Grid>
      </Grid>
    </Box>
  );
};

export default memo(BoxUserProfile);
