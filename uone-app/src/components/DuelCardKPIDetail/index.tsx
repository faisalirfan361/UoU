import React from "react";
import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";
import DuelCardKPIDetailProps from "./types";
import Typography from "@material-ui/core/Typography";
import useDuelKPIDetailStyle from "./style";

const DuelCardKPIDetail: React.FC<DuelCardKPIDetailProps> = ({
  kpi,
  duration,
}) => {
  const classes = useDuelKPIDetailStyle();

  return (
    <Box display="flex" p={1} mt={1} width={1}>
      <Grid container spacing={1}>
        <Grid item xs={6}>
          <Box width={1}>
            <Typography className={classes.KpiDurationHeading}>KPI</Typography>
          </Box>
          <Box width={1}>
            <Typography className={classes.KpiDurationData}>{kpi}</Typography>
          </Box>
        </Grid>
        <Grid item xs={6}>
          <Box width={1}>
            <Typography className={classes.KpiDurationHeading}>
              DURATION
            </Typography>
          </Box>
          <Box width={1}>
            <Typography className={classes.KpiDurationDataBold}>
              {duration} {duration === "0" && "min"} remaining
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DuelCardKPIDetail;
