import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";

import Style from "./style";
import IProps from "./kpiPerformanceBarsTypes";

import { SliderMetricComponent } from "../../../index";
import { performanceState } from "../../../../constants";
import { useStatusColor } from "../../../../utils/kpiStatusHelper";
import { sortBy, orderBy } from "lodash";

const getValPercentage = function (obtained: number, goal: number) {
  const obtainedCheckedVal = obtained > goal ? goal : obtained;
  return Math.floor((obtainedCheckedVal / goal) * 100);
};
const calculateStatus = function (percentage: number) {
  if (percentage < 50) {
    return performanceState.behind;
  } else if (percentage < 100) {
    return performanceState.onTrack;
  } else {
    return performanceState.complete;
  }
};

const KpiPerformanceBars: FC<IProps> = ({ kpiSettings }) => {
  const classes = Style();
  const { getStatusColor } = useStatusColor();
  const sortedKpiSettings = orderBy(kpiSettings, "title");

  return (
    <Box className={classes.root}>
      {sortedKpiSettings.map((kpiSetting, index: number) => {
        const kpiValPercentage = getValPercentage(
          kpiSetting.obtained,
          kpiSetting.goal
        );
        const kpiStatus = calculateStatus(kpiValPercentage);
        const kpiStatusColor = getStatusColor(kpiStatus);

        return (
          <SliderMetricComponent
            key={index}
            label={kpiSetting.code}
            description={`${kpiSetting.title}`}
            value={kpiValPercentage}
            statusColor={kpiStatusColor}
            goal={`${kpiSetting.goal}`}
            obtained={`${kpiValPercentage}`}
          />
        );
      })}
    </Box>
  );
};

export default memo(KpiPerformanceBars);
