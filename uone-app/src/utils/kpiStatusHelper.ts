import { Theme } from "@material-ui/core/styles";
import { useTheme } from "@material-ui/styles";

import { performanceState } from "../constants";

export const useStatusColor = () => {
  const theme = useTheme<Theme>();
  const getStatusColor = (status: string) => {
    switch (status) {
      case performanceState.behind:
        return theme.common.uoneShineViolet[500];
      case performanceState.onTrack:
        return theme.common.uonePaleYellow[500];
      default:
        return theme.common.uoneNeonGreen[500];
    }
  };
  return {
    getStatusColor,
  };
};

export const getUserStatus = (kpiSettings: any) => {
  let averagePercentage = 0;

  for (const kpi of kpiSettings) {
    const obtained = kpi.obtained > kpi.goal ? kpi.goal : kpi.obtained;
    averagePercentage += Math.floor((obtained / kpi.goal) * 100);
  }

  averagePercentage = averagePercentage / kpiSettings.length;

  if (averagePercentage < 50) {
    return performanceState.behind;
  } else {
    return performanceState.onTrack;
  }
};

export const getTeamStatus = (kpis: any) => {
  let averagePercentage = 0;

  for (const kpi of kpis) {
    const settings = kpi.settings;
    const obtained =
      settings.optained > settings.goal ? settings.goal : settings.optained;
    averagePercentage += Math.floor((obtained / settings.goal) * 100);
  }

  averagePercentage = averagePercentage / kpis.length;

  if (averagePercentage < 50) {
    return performanceState.behind;
  } else {
    return performanceState.onTrack;
  }
};
