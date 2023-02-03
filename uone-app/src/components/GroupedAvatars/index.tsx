import React, { FC } from "react";

import IProps from "./types";
import useStyles from "./style";
import StyledAvatar from "components/StyledAvatar";

const GroupedAvatarsComponent: FC<IProps> = ({ images }) => {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <div
        className={`${classes.avatarSecondaryContainer}
                        ${classes.avatarLeftContainer}
                        ${images[0].cssClass}`}
      >
        <StyledAvatar
          src={images[0].imgSrc}
          alt={images[0].alt}
          className={`${classes.avatar}`}
        />
      </div>
      <div
        className={`${classes.avatarPrimaryContainer} ${images[1].cssClass}`}
      >
        <StyledAvatar
          src={images[1].imgSrc}
          alt={images[1].alt}
          className={`${classes.avatar}`}
        />
      </div>
      <div
        className={`${classes.avatarSecondaryContainer}
                      ${classes.avatarRightContainer}
                      ${images[2].cssClass}`}
      >
        <StyledAvatar
          src={images[2].imgSrc}
          alt={images[2].alt}
          className={`${classes.avatar}`}
        />
      </div>
    </div>
  );
};

export default GroupedAvatarsComponent;
