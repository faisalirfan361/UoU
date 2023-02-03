import React, { FC } from "react";
import { Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";

import { ChallengeBlankRowProps } from "./types";
import ZebraTableRow from "components/TableComponents/ZebraTableRow";

const ChallengeBlankRow: FC<ChallengeBlankRowProps> = ({ rowNumber }) => {
  return (
    <ZebraTableRow >
      <TableCell>-</TableCell>
      <TableCell>
        <Typography>-</Typography>
      </TableCell>
      <TableCell>-</TableCell>
    </ZebraTableRow>
  );
};

export default ChallengeBlankRow;
