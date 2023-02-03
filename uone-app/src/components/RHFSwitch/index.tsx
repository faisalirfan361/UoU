import React, { FC } from "react";
import { Controller } from "react-hook-form";
import Switch from "@material-ui/core/Switch";

import IProps from "./types";
// import useStyle from "./style";

const RHFSwitchComponent: FC<IProps> = ({ name, control, defaultValue }) => {
  // const classes = useStyle();

  return (
    <>
      <Controller
        name={name}
        control={control}
        defaultValue={defaultValue}
        render={(props) => (
          <Switch
            id={`id-switch-${name}`}
            onChange={(e) => props.onChange(e.target.checked)}
            checked={props.value}
          />
        )}
      />
    </>
  );
};

export default RHFSwitchComponent;
