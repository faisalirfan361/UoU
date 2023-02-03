/**
 * Please read:
 *  - https://github.com/iulian-radu-at/react-select-material-ui
 *
 * About Options Prop:
 *  - https://github.com/iulian-radu-at/react-select-material-ui#fields-defined-by-selectoption
 */
import React, { FC, useState } from "react";
import { Controller } from "react-hook-form";
import Autocomplete from "@material-ui/lab/Autocomplete";
import IProps from "./types";
import useStyles from "./style";
import TextField from "@material-ui/core/TextField";
import Paper from "@material-ui/core/Paper";
import InfiniteScroll from "react-infinite-scroll-component";
import CircularProgress from "@material-ui/core/CircularProgress";

const PaperComponent: React.FC = ({ children }) => {
  const classes = useStyles();
  return <Paper className={classes.selectPapper}>{children}</Paper>;
};

const ListBoxComponent: React.FC = ({ children, ...restProps }) => {
  const classes = useStyles();
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
const RHFSingleSelectComponent: FC<IProps> = ({
  control,
  name,
  defaultValue,
  placeholder,
  label,
  options = [],
  errors = null,
  style = null,
  hasMore = false,
  loadMore = () => {},
}) => {
  const classes = useStyles();
  const [selectedValue, setSelectedValue] = useState(
    defaultValue ? defaultValue : null
  );
  let isError = false;
  let errorMessage = "";

  if (errors && errors.hasOwnProperty(name)) {
    isError = true;
    errorMessage = errors[name].message;
  }

  const Loading = (
    <div className={classes.loading}>
      <CircularProgress className={classes.CircularProgress} />
    </div>
  );
console.log(">>>>>>>", {hasMore});

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
        defaultValue={defaultValue}
        render={({ onChange, value }) => {
          return (
            <Autocomplete
              fullWidth={true}
              // id={`id-single-select-${name}`}
              value={selectedValue}
              options={options}
              onChange={(e, data) => {
                if (data) {
                  setSelectedValue(data);
                  onChange(data.value);
                } else {
                  setSelectedValue(null);
                  onChange(null);
                }
              }}
              PaperComponent={PaperComponent}
              ListboxComponent={ListBoxComponent}
              ListboxProps={{
                id: `id-single-select-${name}`,
                scrollProps: {
                  dataLength: options.length,
                  next: loadMore,
                  hasMore: hasMore,
                  loader: Loading,
                  scrollableTarget: `id-single-select-${name}`,
                },
              }}
              getOptionLabel={(option: any) => (option ? option.label : "")}
              getOptionSelected={(option: any) =>
                option.value == selectedValue.value
              }
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
          );
        }}
      />
    </>
  );
};

export default RHFSingleSelectComponent;
