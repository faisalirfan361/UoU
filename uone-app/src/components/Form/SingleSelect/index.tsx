/**
 * Please read:
 *  - https://github.com/iulian-radu-at/react-select-material-ui
 *
 * About Options Prop:
 *  - https://github.com/iulian-radu-at/react-select-material-ui#fields-defined-by-selectoption
 */
import React, { FC } from "react";
import { SingleSelect } from "react-select-material-ui";
import Grid from "@material-ui/core/Grid";

import IProps from "./types";
import useStyles from "./style";

const FormSingleSelectComponent: FC<IProps> = ({
  value,
  placeholder,
  options,
  handleOnChange,
  ...rest
}) => {
  const classes = useStyles();

  return (
    <Grid container className={classes.selectRoleBox}>
      <Grid item xs={12}>
        <SingleSelect
          placeholder={placeholder}
          options={options}
          onChange={handleOnChange}
          SelectProps={{
            // https://react-select.com/styles#style-object
            styles: {
              option: (provided: any, state: any) => ({
                ...provided,
                // color: state.isSelected ? "white" : "black",
                padding: 14,
              }),

              control: () => ({
                width: 120,
                border: "none",
                "&:hover": {
                  border: "none",
                },
              }),

              dropdownIndicator: (base: any) => ({
                ...base,
                color: "red", // Custom colour
              }),

              indicatorsContainer: (provided: any) => ({
                ...provided,
                color: "#F8AB3D",
              }),

              indicatorSeparator: () => ({
                border: "none",
              }),

              singleValue: (provided: any, state: any) => {
                const opacity = state.isDisabled ? 0.5 : 1;
                const transition = "opacity 300ms";

                return { ...provided, opacity, transition };
              },
            },
            // https://react-select.com/styles#overriding-the-theme
            theme: (theme: { colors: any }) => {
              return {
                ...theme,
                borderRadius: 0,
                colors: {
                  ...theme.colors,
                  primary25: "#FCD248",
                  primary: "#F8AB3D",
                  primary50: "#F8AB3D",
                  primary75: "#F8AB3D",
                },
              };
            },
          }}
          {...rest}
        />
      </Grid>
    </Grid>
  );
};

export default FormSingleSelectComponent;
