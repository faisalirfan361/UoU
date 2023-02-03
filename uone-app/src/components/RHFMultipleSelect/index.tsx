/**
 * Please read:
 *  - https://github.com/iulian-radu-at/react-select-material-ui
 *
 * About Options Prop:
 *  - https://github.com/iulian-radu-at/react-select-material-ui#fields-defined-by-selectoption
 */
import React, { FC } from "react";
import { Controller } from "react-hook-form";
import { MultipleSelect } from "react-select-material-ui";

import RHFMultiSlectProps from "./types";
import useStyles from "./style";

const RHFMultipleSelectComponent: FC<RHFMultiSlectProps> = ({
  control,
  name,
  defaultValues,
  placeholder,
  label,
  options,
  errors = null,
}) => {
  const classes = useStyles();

  let isError = false;
  let errorMessage = "";

  if (errors && errors.hasOwnProperty(name)) {
    isError = true;
    errorMessage = errors[name].message;
  }

  return (
    <>
      <Controller
        name={name}
        control={control}
        defaultValues={defaultValues}
        SelectProps={{
          isClearable: true,
          msgNoOptionsAvailable: "All options are selected",
          msgNoOptionsMatchFilter: "No option matches the filter",
        }}
        render={({ onChange, value }) => (
          <MultipleSelect
            className={classes.select}
            id={`id-multi-select-${name}`}
            options={options}
            onChange={onChange}
            values={value}
            error={isError}
            helperText={errorMessage}
            label={label}
            placeholder={placeholder}
            fullWidth
          />
        )}
      />
    </>
  );
};

export default RHFMultipleSelectComponent;
