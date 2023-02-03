import React, { FC, useState } from "react";
import Button from "@material-ui/core/Button";

import ButtonDuelProps from "./types";
import useButtonDuelStyles from "./style";

const ButtonActionComponent: FC<ButtonDuelProps> = ({
  children,
  handleOnClick,
  disabled = false,
}) => {
  const classes = useButtonDuelStyles();

  return (
    <Button
      classes={classes}
      variant="outlined"
      color="primary"
      onClick={handleOnClick}
      disabled={disabled}
    >
      {children}
    </Button>
  );
};

export default ButtonActionComponent;
