import React, { FC } from "react";
import Button from "@material-ui/core/Button";

import IProps from "./types";
import useStyles from "./style";

const ButtonActionComponent: FC<IProps> = ({ children, handleOnClick, disabled= false }) => {
  const classes = useStyles();

  const onClick = () => {
    handleOnClick();
  };

  return (
    <Button
      classes={{
        outlinedPrimary: classes.outlinedPrimary,
      }}
      variant="outlined"
      color="primary"
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </Button>
  );
};

export default ButtonActionComponent;
