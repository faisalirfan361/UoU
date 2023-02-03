import { Metric } from "hooks/useMetrics";

export interface MetricsSelectorProps {
  options: Metric[];
  defaultOption?: Metric;
  onSelect: (metric: Metric) => void;
  label: string;
}
