import React from "react";
import { storiesOf } from "@storybook/react";
import { useForm } from "react-hook-form";

import RHFSliderComponent from ".";

storiesOf("RHFSliderComponent", module).add("basic", () => {
  const { control } = useForm({});

  return (
    <RHFSliderComponent
      control={control}
      name="goal"
      step={1}
      max={100}
      defaultValue={50}
      label="KPI"
      setChangeCommitted={() => true}
    />
  );
});
