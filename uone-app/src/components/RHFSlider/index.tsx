import React, { FC } from "react";
import { Controller } from "react-hook-form";
import Slider from "@material-ui/core/Slider";

import IProps from "./types";
// import useStyle from "./style";

const RHFSliderComponent: FC<IProps> = ({
  name,
  control,
  defaultValue,
  max,
  step,
  setChangeCommitted,
}) => {
  // const classes = useStyle();

  return (
    <>
      <Controller
        name={name}
        control={control}
        defaultValue={defaultValue}
        render={(props) => (
          <Slider
            id={`id-slider-${name}`}
            {...props}
            onChange={(_, value) => {
              setChangeCommitted(false);
              props.onChange(value);
            }}
            onChangeCommitted={(_, value) => {
              setChangeCommitted(true);
              props.onChange(value);
            }}
            valueLabelDisplay="auto"
            max={max}
            step={step}
          />
        )}
      />
    </>
  );
};

export default RHFSliderComponent;
