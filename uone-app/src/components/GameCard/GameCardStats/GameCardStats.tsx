import { Grid } from "@material-ui/core";
import { FC } from "react";

export interface GameCardStatsProps {}

const GameCardStats: FC<GameCardStatsProps> = ({ children }) => {
  return (
    <Grid container spacing={2}>
      {children}
    </Grid>
  );
};

export default GameCardStats;
