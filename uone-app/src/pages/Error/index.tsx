import React, { FC, memo } from "react";

import Props from "./types";

const Error: FC<Props> = ({ status, error }) => {
  return (
    <>
      Error {`${status}`} - {`${error}`}
    </>
  );
};

export default memo(Error);
