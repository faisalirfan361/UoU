import { Indicator } from "hooks/useIndicators";

export const goals = [
  {
    id: "2",
    indicators: <Indicator[]>[],
    metricType: "Number",
    duration: "Daily",
    weight: 100,
    points: 1000,
    minNumber: 1000,
    maxNumber: 3600,
    goalVal: 2000,
    minInfinite: false,
    maxInfinite: false,
    flipRange: false,
  },
  {
    id: "1",
    indicators: <Indicator[]>[],
    metricType: "Number",
    duration: "Weekly",
    weight: 20,
    points: 100,
    minNumber: 0,
    maxNumber: 100,
    goalVal: 10,
    minInfinite: false,
    maxInfinite: true,
    flipRange: false,
  },
  {
    id: "2",
    indicators: <Indicator[]>[],
    metricType: "Seconds",
    duration: "Weekly",
    weight: 50,
    points: 1000,
    minNumber: 0,
    maxNumber: 100,
    goalVal: 3600,
    minInfinite: false,
    maxInfinite: true,
    flipRange: true,
  },

  {
    id: "5",
    indicators: <Indicator[]>[],
    metricType: "Number",
    duration: "Daily",
    weight: 100,
    points: 1000,
    minNumber: 0,
    maxNumber: 15,
    goalVal: 5,
    minInfinite: false,
    maxInfinite: false,
    flipRange: true,
  },
];
