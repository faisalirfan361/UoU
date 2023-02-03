import { Box, Grid, IconButton } from "@material-ui/core";
import { FaCircle } from "react-icons/fa";
import clsx from "clsx";

import performanceBadgesStyles from "./styles";

export default function PerformanceBadgesDots({
  page = 1,
  count,
  onChange,
}: any) {
  const classes = performanceBadgesStyles();
  const dots = new Array(count).fill(null);
  return (
    <Grid container spacing={1} justify="center">
      {dots.map((_, index) => (
        <Grid item key={`badge-dot-${index}`}>
          <IconButton
            onClick={(e) => onChange(e, index + 1)}
            className={clsx(classes.dotsButton, {
              [classes.dotsButtonActive]: page === index + 1,
            })}
          >
            <FaCircle />
          </IconButton>
        </Grid>
      ))}
    </Grid>
  );
}
