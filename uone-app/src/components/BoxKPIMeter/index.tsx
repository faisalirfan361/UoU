import React, { FC, memo } from "react";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import Box from "@material-ui/core/Box";

import IProps from "./types";

import useStyles from "./style";

const BoxUserProfile: FC<IProps> = ({ children }) => {
  const classes = useStyles();

  if (!children) return null;

  return (
    <Box className={classes.root}>
      <Card className={classes.card}>
        <CardContent className={classes.cardContent}>{children}</CardContent>
      </Card>
    </Box>
  );
};

export default memo(BoxUserProfile);
