import React, { FC } from "react";
import Card from "@material-ui/core/Card";
import CardHeader from "@material-ui/core/CardHeader";
import Avatar from "@material-ui/core/Avatar";
import Typography from "@material-ui/core/Typography";

import pointsSVG from "../../assets/img/points.svg";

import IProps from "./types";
import useStyles from "./style";
import StyledAvatar from "components/StyledAvatar";

const CardSimpleComponent: FC<IProps> = ({
  singleAvatar,
  statusColor,
  title,
  subtitle,
  points,
}) => {
  const overrideProps = { statusColor };

  const classes = useStyles(overrideProps)();

  let avatar = null;

  if (!singleAvatar) {
    avatar = (
      <StyledAvatar alt={subtitle} className={classes.headerRootAvatar} />
    );
  } else {
    avatar = (
      <StyledAvatar
        alt={subtitle}
        src={singleAvatar}
        className={classes.headerRootAvatar}
      />
    );
  }

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

  const pointsText = (
    <Typography
      className={classes.headerRootAction}
      variant="subtitle2"
      gutterBottom
    >
      <img
        className={classes.headerRootActionPoints}
        src={pointsSVG}
        alt="Points"
        width="20"
        height="20"
      />
      {`${points} Points`}
    </Typography>
  );

  return (
    <Card classes={{ root: classes.cardRoot }}>
      <CardHeader
        avatar={avatar}
        action={pointsText}
        title={titleText}
        subheader={subtitleText}
        classes={{
          root: classes.headerRoot,
        }}
      />
    </Card>
  );
};

export default CardSimpleComponent;
