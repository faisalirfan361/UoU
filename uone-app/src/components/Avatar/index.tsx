import React, { FC, useState, useEffect } from "react";

import IProps from "./types";
import useStyle from "./style";
import config from "../../config";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import StyledAvatar from "components/StyledAvatar";

const AvatarComponent: FC<IProps> = ({
  initials,
  alt,
  src,
  backgroundColor,
  dimension,
  extraStyles,
  className = "",
  children,
}) => {
  const styles = useStyle(backgroundColor, dimension)();
  const { avatarImages } = useRecoilValue(userAtom);

  const imgPath = `${config.targetBucketUrl}${avatarImages?.keys.large}`;

  return (
    <StyledAvatar
      alt={alt}
      src={imgPath}
      className={`${className} ${styles.avatar}`}
      style={extraStyles}
    >
      {initials || children}
    </StyledAvatar>
  );
};

export default AvatarComponent;
