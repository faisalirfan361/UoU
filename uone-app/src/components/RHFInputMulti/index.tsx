import React, { FC } from "react";
import { Controller } from "react-hook-form";
import TextField from "@material-ui/core/TextField";

import RHFInputMultiProps from "./types";
import useRHFInputMultiStyle from "./style";

const RHFInputComponent: FC<RHFInputMultiProps> = ({
  name,
  control,
  type,
  defaultValue,
  variant = "outlined",
  label,
  inputProps = {
    min: 0,
  },
  errors = null,
}) => {
  const classes = useRHFInputMultiStyle();

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
        multiline
        rows={6}
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
      />
    </>
  );
};

export default RHFInputComponent;
