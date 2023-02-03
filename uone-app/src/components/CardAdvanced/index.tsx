import React, { FC } from "react";
import Card from "@material-ui/core/Card";
import CardHeader from "@material-ui/core/CardHeader";
import CardContent from "@material-ui/core/CardContent";
import Divider from "@material-ui/core/Divider";
import Typography from "@material-ui/core/Typography";

import IProps from "./types";
import useStyles from "./style";

const CardAdvancedComponent: FC<IProps> = ({
  children,
  groupedAvatars,
  statusColor,
  title,
  subtitle,
  status,
}) => {
  const overrideProps = { statusColor };

  const classes = useStyles(overrideProps)();

  const titleText = (
    <Typography
      className={classes.headerRootTitle}
      variant="subtitle2"
      gutterBottom
    >
      {title}
    </Typography>
  );

  const subtitleText = (
    <Typography
      className={classes.headerRootSubtitle}
      variant="subtitle2"
      gutterBottom
    >
      {subtitle}
    </Typography>
  );

  return (
    <Card classes={{ root: classes.cardRoot }}>
      <CardHeader
        avatar={groupedAvatars}
        action={status}
        title={titleText}
        subheader={subtitleText}
        classes={{
          root: classes.headerRoot,
          avatar: classes.headerRootAvatar,
          action: classes.headerRootAction,
        }}
      />
      <CardContent classes={{ root: classes.cardContent }}>
        <Divider classes={{ root: classes.cardContentDivider }} />
        {children}
      </CardContent>
    </Card>
  );
};

export default CardAdvancedComponent;
