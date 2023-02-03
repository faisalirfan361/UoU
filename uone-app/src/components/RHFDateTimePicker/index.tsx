import React, { FC } from "react";
import { Controller } from "react-hook-form";
import DateFnsUtils from "@date-io/date-fns"; // choose your lib
import { MuiPickersUtilsProvider, DateTimePicker } from "@material-ui/pickers";

import IProps from "./types";
// import useStyle from "./style";

const RHFDateTimePickerComponent: FC<IProps> = ({
  name,
  control,
  label,
  variant = "outlined",
  format,
  errors = null,
  defaultValue = "",
  disable = false,
}) => {
  // const classes = useStyle();

  let isError = false;
  let errorMessage = "";

  if (errors && errors.hasOwnProperty(name)) {
    isError = true;
    errorMessage = errors[name].message;
  }

  return (
    <>
      <MuiPickersUtilsProvider utils={DateFnsUtils}>
        <Controller
          name={name}
          control={control}
          format={format}
          inputVariant={variant}
          variant="dialog"
          render={({ onChange, value }) => (
            <DateTimePicker
              fullWidth
              id={`id-datetime-picker-${name}`}
              onChange={onChange}
              value={value}
              label={label}
              error={isError}
              disabled={disable}
              helperText={errorMessage}
              InputProps={{
                disableUnderline: true,
              }}
            />
          )}
        />
      </MuiPickersUtilsProvider>
    </>
  );
};

export default RHFDateTimePickerComponent;
