import React, { FC, useState } from "react";
import Button from "@material-ui/core/Button";

import ButtonOutlinedProps from "./types";
import useButtonOutlinedStyles from "./style";

const ButtonOutlinedComponent: FC<ButtonOutlinedProps> = ({
  children,
  handleOnClick,
  disabled = false,
}) => {
  const classes = useButtonOutlinedStyles();

  return (
    <Button
      classes={{
        outlinedPrimary: classes.outlinedPrimary,
      }}
      variant="outlined"
      color="primary"
      onClick={handleOnClick}
      disabled={disabled}
    >
      {children}
    </Button>
  );
};

export default ButtonOutlinedComponent;
