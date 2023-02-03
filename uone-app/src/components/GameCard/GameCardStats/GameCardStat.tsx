import { Grid, Typography } from "@material-ui/core";
import { FC } from "react";

import useGameCardStatsStyles from "./style";

export interface GameCardStatProps {
  label: string;
  [name: string]: any;
}

const GameCardStat: FC<GameCardStatProps> = ({ label, children, ...props }) => {
  const statStyles = useGameCardStatsStyles();

  return (
    <Grid item container direction="column" style={{ minWidth: 0 }} {...props}>
      <Grid item className={statStyles.label}>
        <Typography variant="caption" noWrap>
          {label}
        </Typography>
      </Grid>
      <Grid item className={statStyles.value}>
        {children}
      </Grid>
    </Grid>
  );
};

export default GameCardStat;
