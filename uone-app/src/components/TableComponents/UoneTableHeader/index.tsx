import React, { FC } from "react";
import { withStyles } from '@material-ui/core/styles';
import TableCell from '@material-ui/core/TableCell';
import clsx from "clsx";

import UOneTableHeaderProps from "./types";

const UOneTableHeader: FC<UOneTableHeaderProps> = ({ cssClass, children }) => {

  const StyledTableHeader = withStyles((theme) => ({
    head: {
      backgroundColor: theme.palette.common.white,
      color: theme.palette.common.black,
      textTransform: "uppercase",
      "& p": {
        fontSize: 11,
        fontWeight: 500,
      }
    },
  }))(TableCell);

  return (
    <StyledTableHeader className={clsx({ cssClass })}>
      {children}
    </StyledTableHeader>
  );
};

export default UOneTableHeader;
