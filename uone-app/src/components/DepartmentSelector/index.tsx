import React, {useState} from "react";

import Autocomplete from "@material-ui/lab/Autocomplete";
import TextField from "@material-ui/core/TextField";

import {Style, autoCompleteStyle} from "./style";
import {DepartmentSelectorProps, Department} from "./types";

const DepartmentSelector: React.FC<DepartmentSelectorProps> = ({
  options,
  defaultOption,
  onSelect,
}) => {

  const classes = Style();
  const autocompleteClasses = autoCompleteStyle();
  const [selectedDepartment, setSelectedDepartment] = useState<Department>(defaultOption);

  const onSelectDepartment = (dep: Department)=>{
    if(!dep){
      onSelect(defaultOption);
      setSelectedDepartment(defaultOption);
    }else{
      onSelect(dep);
      setSelectedDepartment(dep);
    }
  };

  return (
    <div className={classes.root}>
      <label className={classes.dropdownLabel}>Department</label>
      <Autocomplete
        id="combo-box-deps"
        onChange={(event, dep) =>
          onSelectDepartment(dep as Department) }
        options={[defaultOption].concat(options)}
        getOptionLabel={(option: any) => option.dname}
        classes={autocompleteClasses}
        value={selectedDepartment}
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

export default DepartmentSelector;
