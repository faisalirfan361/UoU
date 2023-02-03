// @ts-nocheck
import React, { FC, memo, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { useSnackbar } from "notistack";
import _get from "lodash.get";

import Error from "pages/Error";

import Props from "./types";

/**
 * Not exactly a Context, but a wrapper.
 */
const ErrorHandler: FC<Props> = ({ children }) => {
  const { enqueueSnackbar } = useSnackbar();
  const location = useLocation();

  const error = _get(location, "state.error", "");
  const status = _get(location, "state.status", 0);

  useEffect(() => {
    if (error) {
      enqueueSnackbar(error, {
        variant: "error",
        autoHideDuration: 3000,
      });
    }
  }, []);

  switch (true) {
    case status >= 400:
      return <Error status={status} error={`API - Error: ${error}`} />;

    // Add new cases for other types of errors here.

    default:
      return children;
  }
};

export default memo(ErrorHandler);
