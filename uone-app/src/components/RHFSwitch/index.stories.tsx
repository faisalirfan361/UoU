import React from "react";
import { storiesOf } from "@storybook/react";
import { useForm } from "react-hook-form";

import RHFSwitchComponent from ".";

storiesOf("RHFSwitchComponent", module).add("basic", () => {
  const { control } = useForm({});

  return (
    <RHFSwitchComponent control={control} name="isRange" defaultValue={true} />
  );
});
