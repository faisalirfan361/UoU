import React, { useState } from "react";

import Autocomplete from "@material-ui/lab/Autocomplete";
import TextField from "@material-ui/core/TextField";
import { Style, autoCompleteStyle } from "./style";
import { FilterSelectorProps } from "./types";
import { Grid } from "@material-ui/core";

const FilterSelector: React.FC<FilterSelectorProps> = ({
  options,
  defaultOption,
  onSelect,
  label,
}) => {
  const classes = Style();
  const autocompleteClasses = autoCompleteStyle();
  const [selectedFilter, setSelectedFilter] = useState(defaultOption);

  const onSelectChange = (value: any) => {
    if (!value) {
      onSelect(defaultOption);
      setSelectedFilter(defaultOption);
    } else {
      onSelect(value);
      setSelectedFilter(value);
    }
  };

  return (
    <Grid container>
      <Grid item xs={4}>
        <label className={classes.selectorLabel}>{label}</label>
      </Grid>
      <Grid item xs={8}>
        <Autocomplete
          id="combo-box-filter"
          onChange={(event, value) => onSelectChange(value)}
          options={options}
          getOptionLabel={(option: string) => option}
          classes={autocompleteClasses}
          value={selectedFilter}
          renderInput={(params) => (
            <TextField
              {...params}
              InputLabelProps={{ shrink: false }}
              InputProps={{ ...params.InputProps, disableUnderline: true }}
            />
          )}
        />
      </Grid>
    </Grid>
  );
};

export default FilterSelector;
