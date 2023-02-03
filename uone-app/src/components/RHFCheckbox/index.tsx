import { FC } from "react";
import { Controller } from "react-hook-form";
import Checkbox from "@material-ui/core/Checkbox";
import FormControlLabel from "@material-ui/core/FormControlLabel";

import { RHFCheckboxProps } from "./types";
import useRHFCheckboxStyles from "./style";

const RHFRadioButtonComponent: FC<RHFCheckboxProps> = ({
  control,
  name,
  defaultValue,
  value,
  label,
  errors = null,
  externalOnChange = null,
  disabled = false,
}) => {
  const classes = useRHFCheckboxStyles();

  let isError = false;
  let errorMessage = "";

  if (errors && errors.hasOwnProperty(name)) {
    isError = true;
    errorMessage = errors[name].message;
  }

  return (
    <Controller
      name={name}
      control={control}
      defaultValue={defaultValue}
      render={({ onChange }) => (
        <FormControlLabel
          control={
            <Checkbox
              checked={value}
              disabled={disabled}
              onChange={(e) => {
                onChange(e.target.checked);
                if (externalOnChange) externalOnChange(e.target.checked);
              }}
              name={name}
              color="primary"
            />
          }
          label={label}
        />
      )}
    />
  );
};

export default RHFRadioButtonComponent;
