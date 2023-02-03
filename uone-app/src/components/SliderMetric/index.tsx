import React, { FC } from "react";
import Grid from "@material-ui/core/Grid";
import Slider from "@material-ui/core/Slider";
import Tooltip from "@material-ui/core/Tooltip";
import Typography from "@material-ui/core/Typography";

import SliderMetricProps from "./types";
import useSliderMetricStyles from "./style";

const SliderMetricComponent: FC<SliderMetricProps> = ({
  label,
  description,
  value,
  statusColor,
  goal,
  obtained,
}) => {
  const background = statusColor;
  const percentage = value;
  const classes = useSliderMetricStyles({
    statusColor,
    background,
    percentage,
  });

  return (
    <Grid container spacing={2}>
      <Grid className={classes.slider} item xs={12}>
        <Tooltip
          classes={{
            tooltip: classes.tooltip,
            popper: classes.popper,
          }}
          title={`${description}: ${obtained} of ${goal} (${value}%)`}
          placement="bottom"
        >
          <div className={classes.percentage}>{label}</div>
        </Tooltip>
      </Grid>
    </Grid>
  );
};

export default SliderMetricComponent;
