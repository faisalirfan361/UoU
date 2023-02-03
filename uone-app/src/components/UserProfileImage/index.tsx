import React, { FC } from "react";
import UserProfileImageProps from "./types";
import {Avatar} from "@material-ui/core";
import useStyles from "./style";



const UserProfileImage: FC<UserProfileImageProps> = ({ imgSrc, className }) => {
  const classes = useStyles();

  //pass width and height in the class if you want to change it
  const propClass = className? className: '';

  return (
    <Avatar src={imgSrc} className={`${propClass} ${classes.root}`} />
  );
};

export default UserProfileImage;
