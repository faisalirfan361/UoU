import React from "react";
import Grid from "@material-ui/core/Grid";
import DuelNameDetailProps from "./types";
import Typography from "@material-ui/core/Typography";
import useStyle from "./style";
import { Chip } from "@material-ui/core";

const STATUS_COLOR = ["#5D7793", "#2FB0D9", "#F25BA4"] as const;
const STATUS_TEXT = ["DRAW", "WINNER", "NEW DUEL CHALLENNGE"] as const;

const DuelCardNameDetail: React.FC<DuelNameDetailProps> = ({
  name,
  coins,
  status,
}) => {
  const classes = useStyle();

  interface statusType {
    text: string;
    color: string;
  }

  const statusContainer: statusType[] = [
    {
      text: "New!",
      color: "#F25BA4",
    },
    {
      text: "Active!",
      color: "#5AD787",
    },
    {
      text: "Draw!",
      color: "#5D7793",
    },
    {
      text: "Winner!",
      color: "#2FB0D9",
    },
  ];

  return (
    <Grid container direction="row" className={classes.duelHeader}>
      <Grid item xs={6}>
        <Typography className={classes.duelNameText}>{name}</Typography>
        <Chip
          size="small"
          label={statusContainer[status].text}
          className={classes.chipTextColor}
          style={{ backgroundColor: `${statusContainer[status].color}` }}
        />
      </Grid>
      <Grid item xs={6}>
        <Typography align="right" className={classes.duelCoinText}>
          {coins} Coins
        </Typography>
      </Grid>
    </Grid>
  );
};

export default DuelCardNameDetail;
