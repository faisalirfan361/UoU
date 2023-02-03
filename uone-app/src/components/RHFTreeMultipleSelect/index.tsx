import React, { FC, useState, useRef, useEffect } from "react";
import { Controller } from "react-hook-form";
import Checkbox from "@material-ui/core/Checkbox";
import TextField from "@material-ui/core/TextField";
import Autocomplete from "@material-ui/lab/Autocomplete";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import CheckBoxIcon from "@material-ui/icons/CheckBox";
import Paper from "@material-ui/core/Paper";
import RHFTreeSelectProps from "./types";
import useRHFTreeSelectStyles from "./style";
import InfiniteScroll from "react-infinite-scroll-component";
import CircularProgress from "@material-ui/core/CircularProgress";
import { useUsers } from "../../hooks/useUsers";

const PaperComponent: React.FC = ({ children }) => {
  const classes = useRHFTreeSelectStyles();
  return <Paper className={classes.selectPapper}>{children}</Paper>;
};

const ListBoxComponent: React.FC = ({ children, ...restProps }) => {
  const classes = useRHFTreeSelectStyles();
  const { scrollProps, ...rest } = restProps as any;
  return (
    <ul {...rest}>
      <InfiniteScroll
        dataLength={scrollProps.dataLength}
        next={scrollProps.next}
        hasMore={scrollProps.hasMore}
        loader={scrollProps.loader}
        scrollableTarget={scrollProps.scrollableTarget}
        className={classes.infiniteScroll}
      >
        {children}
      </InfiniteScroll>
    </ul>
  );
};

const RHFTreeSelectComponent: FC<RHFTreeSelectProps> = ({
  control,
  name,
  defaultValues = null,
  placeholder,
  label,
  options,
  errors = null,
  hasMore = false,
  loadMore = () => {},
}) => {
  const classes = useRHFTreeSelectStyles();
  const [isError, setIsError] = useState(false);
  const [selectAll, setSelectAll] = useState(false);
  const [defV, setDefV] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const selectAllValue = [{ label: " Select All", value: 0 }];
  const [selectedValues, setSelectedValue] = useState(
    defaultValues ? defaultValues : []
  );

  useEffect(() => {
    if (errors && errors.hasOwnProperty(name)) {
      setIsError(true);
      setErrorMessage(errors[name].message);
    }
  }, [errors]);

  const SelectAllHandler = (data: any) => {
    const result = data.find((obj: any) => obj.value === 0);
    if (result) {
      setSelectAll(true);
      return options;
    }
    setSelectAll(false);
    return data;
  };

  const conditionalDefaultValuesProps =
    defaultValues && defaultValues.length > 0
      ? { defaultValue: defaultValues }
      : {};

  const Loading = (
    <div className={classes.loading}>
      <CircularProgress className={classes.CircularProgress} />
    </div>
  );

  return (
    <>
      <Controller
        name={name}
        control={control}
        SelectProps={{
          isClearable: true,
          msgNoOptionsAvailable: "All options are selected",
          msgNoOptionsMatchFilter: "No option matches the filter",
        }}
        render={({ onChange }) => (
          <>
            <Autocomplete
              fullWidth={true}
              multiple
              value={selectedValues}
              options={[...selectAllValue, ...options]}
              PaperComponent={PaperComponent}
              ListboxComponent={ListBoxComponent}
              ListboxProps={{
                id: `scroll-container-${name}`,
                scrollProps: {
                  dataLength: options.length,
                  next: loadMore,
                  hasMore: hasMore,
                  loader: Loading,
                  scrollableTarget: `scroll-container-${name}`,
                },
              }}
              onChange={(_, data) => {
                const result = SelectAllHandler(data);
                setSelectedValue(result);
                onChange(result.map((v: any) => v.value));
              }}
              getOptionLabel={(option: any) => option.label}
              getOptionSelected={(option, value) => {
                return selectAll || option.value == value.value;
              }}
              renderOption={(option: any, { selected }) => (
                <React.Fragment>
                  <Checkbox
                    icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                    checkedIcon={<CheckBoxIcon fontSize="small" />}
                    style={{ marginRight: 8 }}
                    checked={selected}
                  />
                  {option.label}
                  {option.children
                    ? option.children.map((child: any) => (
                        <div>
                          <Checkbox
                            icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                            checkedIcon={<CheckBoxIcon fontSize="small" />}
                            style={{ marginRight: 8 }}
                            checked={selected}
                          />
                          {child.label}
                        </div>
                      ))
                    : null}
                </React.Fragment>
              )}
              renderInput={(params) => (
                <TextField
                  {...params}
                  variant="outlined"
                  label={label}
                  placeholder={placeholder}
                  error={isError}
                  helperText={errorMessage}
                />
              )}
            />
          </>
        )}
      />
    </>
  );
};

export default RHFTreeSelectComponent;