import {
  Line,
  LineChart,
  ResponsiveContainer,
  XAxis,
  CartesianGrid,
  Tooltip,
} from "recharts";
import { PerformanceLineChartProps } from "./type";

export default function PerformanceLineChart({
  data,
  height = 200,
  direction,
}: PerformanceLineChartProps) {
  return (
    <ResponsiveContainer width="100%" height={height}>
      <LineChart data={data} margin={{ top: 0, bottom: 0, left: 0, right: 0 }}>
        <CartesianGrid horizontal={false} strokeDasharray="3 0" />
        <XAxis
          interval="preserveStartEnd"
          tickLine={false}
          dataKey="name"
          type="category"
        />
        <Tooltip />
        <Line
          strokeWidth={3}
          connectNulls
          type="linear"
          dataKey="uv"
          stroke={direction ? "#5AD787" : "#EF647B"}
          fill={direction ? "#5AD787" : "#EF647B"}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
