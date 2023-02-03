import React, { useState } from "react";

import Autocomplete from "@material-ui/lab/Autocomplete";
import TextField from "@material-ui/core/TextField";

import { Style, autoCompleteStyle } from "./style";
import { GameFilterProps, Filter } from "./types";

const defaultOption: Filter = {
  name: "Active",
  value: "active",
};
const options: Filter[] = [
  {
    name: "All",
    value: "",
  },
  {
    name: "Active",
    value: "active",
  },
  {
    name: "Complete",
    value: "complete",
  },
  {
    name: "Draw",
    value: "draw",
  },
];
const GameFilter: React.FC<GameFilterProps> = ({ onSelect }) => {
  const classes = Style();
  const autocompleteClasses = autoCompleteStyle();
  const [selectedFilter, setSelectedFilter] = useState<Filter>(defaultOption);

  const onSelectFilter = (filter: Filter) => {
    if (!filter) {
      onSelect(defaultOption);
      setSelectedFilter(defaultOption);
    } else {
      onSelect(filter);
      setSelectedFilter(filter);
    }
  };

  return (
    <div className={classes.root}>
      <Autocomplete
        id="combo-box-deps"
        onChange={(event, status) => onSelectFilter(status as Filter)}
        options={options}
        getOptionLabel={(option: any) => option.name}
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
    </div>
  );
};

export default GameFilter;
