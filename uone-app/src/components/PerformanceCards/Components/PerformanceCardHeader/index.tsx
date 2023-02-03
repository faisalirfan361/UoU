import React, { FC } from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import clsx from "clsx";

import PerformanceCardHeaderProps from "./PerforCardHeaderTypes";
import Style from "./style";
import LinesEllipsisLoose from "react-lines-ellipsis";

const PerformanceCardHeader: FC<PerformanceCardHeaderProps> = ({
  children,
  status,
  statusColor,
  mainText,
  secondaryText,
  titlesCss,
  deptText,
}) => {
  const classes = Style();

  return (
    <div className={classes.root}>
      <Grid container direction="row" justifyContent="flex-start">
        <Grid item>{children}</Grid>
        <Grid item xs={9} className={clsx(classes.titles, { titlesCss })}>
          <Grid item xs={11}>
            <Typography className={classes.secondaryText} component={"span"}>
              <LinesEllipsisLoose
                text={`${secondaryText} ${deptText || ""}`}
                maxLine="1"
              />
            </Typography>
          </Grid>
          <Typography className={"primaryText"}>{mainText}</Typography>
        </Grid>
      </Grid>

      <span className={classes.status}>
        <span
          className={"statusBall"}
          style={{ backgroundColor: statusColor }}
        ></span>
        <span className={"statusName"}>{status}</span>
      </span>
    </div>
  );
};

export default PerformanceCardHeader;
