import React, { useState } from "react";

import Autocomplete from "@material-ui/lab/Autocomplete";
import TextField from "@material-ui/core/TextField";
import { useMetricSelectorStyles, autoCompleteStyle } from "./style";
import { MetricsSelectorProps } from "./types";
import { Grid } from "@material-ui/core";
import { Metric } from "hooks/useMetrics";

const MetricsSelector: React.FC<MetricsSelectorProps> = ({
  options,
  defaultOption = null,
  onSelect,
  label,
}) => {
  const classes = useMetricSelectorStyles();
  const autocompleteClasses = autoCompleteStyle();
  const defaultMetric: Metric = { id: "-1", name: "Select Metric" };
  const [selectedMetric, setSelectedMetric] = useState<Metric>(defaultMetric);
  const selectOptions = [defaultMetric].concat(options);

  const onSelectChange = (value: any) => {
    if (!value) {
      const tempDefault: Metric = defaultOption ? defaultOption : defaultMetric;
      onSelect(tempDefault);
      setSelectedMetric(tempDefault);
    } else {
      onSelect(value);
      setSelectedMetric(value);
    }
  };

  return (
    <Grid container>
      <Grid item xs={12}>
        <Autocomplete
          id="combo-box-Metrics"
          onChange={(event, value) => onSelectChange(value)}
          options={selectOptions}
          getOptionLabel={(option: Metric) => option.name}
          getOptionSelected={(option, value) => option.id === value.id}
          classes={autocompleteClasses}
          value={selectedMetric}
          fullWidth={true}
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

export default MetricsSelector;
