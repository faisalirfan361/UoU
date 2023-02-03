import { FC } from "react";
import { Controller } from "react-hook-form";
import Radio from "@material-ui/core/Radio";
import RadioGroup from "@material-ui/core/RadioGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import FormControl from "@material-ui/core/FormControl";
import FormLabel from "@material-ui/core/FormLabel";

import { RHFRadioButtonProps, RHFRadioOption } from "./types";
import useRHFRadioButtonStyles from "./style";

const RHFRadioButtonComponent: FC<RHFRadioButtonProps> = ({
  control,
  name,
  defaultValue,
  label,
  options,
  errors = null,
  externalOnChange = null,
  row = false,
  className,
}) => {
  const classes = useRHFRadioButtonStyles();

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
      render={({ onChange, value }) => (
        <FormControl component="fieldset" classes={{ root: className }}>
          <FormLabel component="label">{label}:</FormLabel>
          <RadioGroup
            row={row}
            aria-label={`radio-button-${name}`}
            name={name}
            value={value}
            onChange={(e) => {
              onChange(e);
              if (externalOnChange) externalOnChange(e.target.value);
            }}
          >
            {options.map((option: RHFRadioOption, index: number) => {
              return (
                <FormControlLabel
                  key={`radio-button-${name}-option-${index}`}
                  value={option.value}
                  control={<Radio />}
                  label={option.label}
                />
              );
            })}
          </RadioGroup>
        </FormControl>
      )}
    />
  );
};

export default RHFRadioButtonComponent;
