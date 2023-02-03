import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";
import Tooltip from "@material-ui/core/Tooltip";
import Zoom from "@material-ui/core/Zoom";

import IProps from "./types";
import useStyles from "./style";

const KPIMeter: FC<IProps> = ({ name, label, progress, goal, barColor }) => {
  const classes = useStyles();

  const progressPercent = Math.round(+progress * 100);
  const goalPercent = Math.round(+goal * 100);

  const styleProgressBarProgress = {
    width: `${progressPercent}%`,
    backgroundColor: barColor,
  };

  const styleProgressBarGoal = {
    width: `${goalPercent}%`,
  };

  const tooltip = `${name} ${progressPercent}% of ${goalPercent}%`;

  return (
    <Tooltip
      classes={{ tooltip: classes.tooltip, tooltipArrow: classes.tooltipArrow }}
      title={tooltip}
      placement="top-start"
      TransitionComponent={Zoom}
      arrow
    >
      <Box className={classes.progressBar}>
        <Box className={classes.progressBarProgressBg}>
          <Box
            className={classes.progressBarProgress}
            style={styleProgressBarProgress}
          />
        </Box>
        <Box className={classes.progressBarGoal} style={styleProgressBarGoal} />
        <Box className={classes.progressLabel}>{label}</Box>
      </Box>
    </Tooltip>
  );
};

export default memo(KPIMeter);
