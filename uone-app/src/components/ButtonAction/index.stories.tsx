import React from "react";
import { storiesOf } from "@storybook/react";

import ButtonActionComponent from ".";

const children = "Text";
const handleOnClick = () => console.log("OnClick");

storiesOf("ButtonActionComponent", module).add("basic", () => (
  <ButtonActionComponent handleOnClick={handleOnClick}>
    {children}
  </ButtonActionComponent>
));
