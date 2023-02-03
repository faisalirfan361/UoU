import React, { FC, memo } from "react";
import IProps from "./types";
import useStyles from "./style";
import {LinearProgress} from "@material-ui/core";

const LayoutLoading: FC<IProps> = ({ isInProgress }) => {
  const classes = useStyles();

  return (
    <div className={` ${classes.root} ${ (isInProgress) ? classes.open : '' }`}>
      <LinearProgress className={ `
            ${classes.colorPrimary} ${classes.barColorPrimary}
       `} />
      <LinearProgress className={ `
            ${classes.colorSecondary} ${classes.barColorPrimary}
       `} />
    </div>
  );
};

export default memo(LayoutLoading);
