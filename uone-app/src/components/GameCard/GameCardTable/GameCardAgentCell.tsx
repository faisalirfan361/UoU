import { Box, Grid, TableCell, Typography } from "@material-ui/core";
import StyledAvatar from "components/StyledAvatar";
import useTableStyles from "./styles";
import React from "react";

const GameCardAgentCell = ({ children, row, style, ...props }: any) => {
  const tableStyles = useTableStyles();

  return (
    <TableCell className={tableStyles.agentCell}>
      <Grid container spacing={1}>
        <Grid item>
          <StyledAvatar size={0.8} src={row.avatar} />
        </Grid>
        <Grid item className={tableStyles.agentCellName} style={{ ...style }}>
          {row.fullName}
        </Grid>
      </Grid>
    </TableCell>
  );
};

export default GameCardAgentCell;
