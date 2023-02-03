import React, { FC } from "react";
import Button from "@material-ui/core/Button";

import ButtonSubmitProps from "./types";
import useButtonSubmitPropsStyles from "./style";

const ButtonSubmit: FC<ButtonSubmitProps> = ({ children }) => {
  const classes = useButtonSubmitPropsStyles();

  return (
    <Button
      classes={{
        outlinedPrimary: classes.outlinedPrimary,
      }}
      variant="outlined"
      color="primary"
      type="submit"
    >
      {children}
    </Button>
  );
};

export default ButtonSubmit;
