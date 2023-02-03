import React, { FC } from "react";
import Box from "@material-ui/core/Box";

import IProps from "./cardCTATypes";
import Style from "./style";
import {ButtonOutlinedComponent} from "../../../index";

const CardCTA: FC<IProps> = ({
  ctaText,
  actionFunc,
}) => {

  const classes = Style();

  return (
    <Box className={classes.root}>
      <ButtonOutlinedComponent
        handleOnClick={actionFunc}>
        {ctaText}
      </ButtonOutlinedComponent>
    </Box>
  );
};

export default CardCTA;
