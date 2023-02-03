import React, { FC } from "react";
import { Controller } from "react-hook-form";
import TextField from "@material-ui/core/TextField";

import IProps from "./types";
import useStyle from "./style";

const RHFInputComponent: FC<IProps> = ({
  name,
  control,
  type,
  defaultValue,
  variant = "outlined",
  label,
  inputProps = {
    min: 0,
    max: 50,
  },
  errors = null,
  ...props
}) => {
  const classes = useStyle();

  let isError = false;
  let errorMessage = "";

  if (errors && errors.hasOwnProperty(name)) {
    isError = true;
    errorMessage = errors[name].message;
  }

  return (
    <>
      <Controller
        as={TextField}
        id={`id-input-${name}`}
        control={control}
        name={name}
        type={type}
        defaultValue={defaultValue}
        variant={variant}
        label={label}
        InputLabelProps={{
          classes: {
            root: classes.labelRoot,
            focused: classes.labelFocused,
          },
        }}
        InputProps={{
          inputProps,
          classes: { input: classes.inputClass },
        }}
        error={isError}
        helperText={errorMessage}
        fullWidth
        {...props}
      />
    </>
  );
};

export default RHFInputComponent;
