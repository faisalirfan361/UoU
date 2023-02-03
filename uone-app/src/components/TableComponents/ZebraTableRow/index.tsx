import React, { FC } from "react";
import { withStyles } from '@material-ui/core/styles';
import TableRow from '@material-ui/core/TableRow';

import ZebraTableRowProps from "./types";


const ZebraTableRow: FC<ZebraTableRowProps> = ({className='', children}) => {

  const StyledTableRow = withStyles((theme) => ({
    root: {
      '&:nth-of-type(odd)': {
        backgroundColor: `${theme.common.grey[100]}`
      },
      '&:nth-of-type(even)': {
        backgroundColor: `${theme.palette.common.white}`
      },
    },
  }))(TableRow);

  return (
    <StyledTableRow className={`${className}`} >
      {children}
    </StyledTableRow>
  );
};

export default ZebraTableRow;
