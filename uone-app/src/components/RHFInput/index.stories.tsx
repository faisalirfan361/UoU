import React from "react";
import { storiesOf } from "@storybook/react";
import { useForm } from "react-hook-form";

import RHFInputComponent from ".";

storiesOf("RHFInputComponent", module).add("basic", () => {
  const { control } = useForm({});

  return (
    <RHFInputComponent
      control={control}
      name="max"
      defaultValue={5}
      type="number"
      variant="outlined"
      label="Max"
      inputProps={{
        min: 0,
      }}
    />
  );
});
