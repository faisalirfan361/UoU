import React, { FC } from "react";
import Button from "@material-ui/core/Button";

import AcceptDuelProps from "./types";
import useAcceptDuelStyles from "./style";

const ButtonAcceptDuel: FC<AcceptDuelProps> = ({
  children,
  handleOnClick,
  disabled = false,
}) => {
  const classes = useAcceptDuelStyles();

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

export default ButtonAcceptDuel;
