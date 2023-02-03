import React, { FC } from "react";
import { Avatar } from "@material-ui/core";
import clsx from "clsx";

import useStyles from "./style";
import { StyledAvatarProps } from "./type";

const StyledAvatar: FC<StyledAvatarProps> = ({
  size = 1,
  borderOuterColor = "black",
  borderInnerColor = "white",
  background = "black",
  className = null,
  ...props
}) => {
  const classes = useStyles({
    size,
    borderOuterColor,
    borderInnerColor,
    background,
  });

  return (
    <div className={clsx(classes.wrapper, className)}>
      <Avatar {...props} className={classes.avatar} />
    </div>
  );
};

export default StyledAvatar;
