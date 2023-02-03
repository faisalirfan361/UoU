import React from "react";
import DuelNameDetailActiveProps from "./types";
import useDuelNameDetailActiveStyle from "./style";
import {
  Grid,
  Box,
  Typography,
  useTheme,
  useMediaQuery,
  Chip,
} from "@material-ui/core";

const STATUS_COLOR = "#5AD787" as const;

const DuelCardNameDetail: React.FC<DuelNameDetailActiveProps> = ({
  name,
  coins,
  statusText,
  kpi,
  duration,
}) => {
  const classes = useDuelNameDetailActiveStyle();
  const theme = useTheme();
  const xs = useMediaQuery(theme.breakpoints.up("sm"));
  return (
    <>
      {xs ? (
        <Grid container direction="row" className={classes.duelHeader}>
          <Grid item xs={6}>
            <Typography className={classes.duelNameText}>{name}</Typography>
            <Chip
              size="medium"
              label={`Active!`}
              className={classes.duelStatusActive}
              style={{ backgroundColor: STATUS_COLOR }}
            />
          </Grid>
          <Grid item xs={5}>
            <Grid container spacing={1}>
              <Grid item xs={6}>
                <Box width={1}>
                  <Typography className={classes.KpiDurationHeading}>
                    KPI
                  </Typography>
                </Box>
                <Box width={1}>
                  <Typography className={classes.KpiDurationData}>
                    {kpi}
                  </Typography>
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
                    {duration} remaining
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Grid>
          <Grid item xs={1}>
            <Typography align="right" className={classes.duelCoinText}>
              {coins} Coins
            </Typography>
          </Grid>
        </Grid>
      ) : (
        <Grid container direction="row" className={classes.duelHeader}>
          <Grid
            container
            direction="row"
            justifyContent="space-between"
            alignItems="center"
          >
            <Typography component={"span"} className={classes.duelNameText}>
              {name}
            </Typography>
            <Typography
              component={"span"}
              className={classes.duelStatusActive}
              style={{
                backgroundColor: STATUS_COLOR,
              }}
            >
              {statusText}!
            </Typography>
          </Grid>
          <Grid
            container
            direction="row"
            justifyContent="space-between"
            alignItems="center"
          >
            <Grid
              container
              direction="row"
              justifyContent="space-between"
              spacing={1}
              className={classes.KpiDurationContainer}
            >
              <Grid item xs={4}>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationHeading} ${classes.KpiHeading}`}
                  >
                    KPI
                  </Typography>
                </Box>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationDataBold} ${classes.KpiDuration}`}
                  >
                    {kpi}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationHeading} ${classes.KpiHeading}`}
                  >
                    DURATION
                  </Typography>
                </Box>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationDataBold} ${classes.KpiDuration}`}
                  >
                    {duration} min remaining
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationHeading} ${classes.KpiHeading}`}
                  >
                    Coins
                  </Typography>
                </Box>
                <Box width={1}>
                  <Typography
                    className={`${classes.KpiDurationDataBold} ${classes.KpiDuration}`}
                  >
                    {coins}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      )}
    </>
  );
};

export default DuelCardNameDetail;
