import React, { FC } from "react";
import Button from "@material-ui/core/Button";

import ButtonDeleteItemProps from "./types";
import useButtonDeleteItemStyles from "./style";

const ButtonActionComponent: FC<ButtonDeleteItemProps> = ({
  children,
  handleOnClick,
  disabled = false,
}) => {
  const classes = useButtonDeleteItemStyles();

  const onClick = () => {
    handleOnClick();
  };

  return (
    <Button
      classes={classes}
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
