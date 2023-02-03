import { Typography, Box } from "@material-ui/core";
import { TrendingDownArrow } from "components/Icons/TrendingDownArrow";
import { TrendingUpArrow } from "components/Icons/TrendingUpArrow";
import { PerformanceLineTrendProps } from "./type";

export default function PerformanceLineTrend({
  direction,
}: PerformanceLineTrendProps) {
  if (direction) {
    return (
      <div>
        <Typography variant="subtitle2">Trending</Typography>
        <Box fontSize={32}>
          <TrendingUpArrow fill={`url(#trending-arrow-up-)`}>
            <defs>
              <linearGradient id={`trending-arrow-up-`}>
                <stop offset="-3%" style={{ stopColor: "#5AD787" }} />
                <stop offset="100%" style={{ stopColor: "#5AD787" }} />
              </linearGradient>
            </defs>
          </TrendingUpArrow>
        </Box>
        <Typography variant="subtitle2">Up</Typography>
      </div>
    );
  } else {
    return (
      <div>
        <Typography variant="subtitle2">Trending</Typography>
        <Box fontSize={32}>
          <TrendingDownArrow fill={`url(#trending-arrow-down-)`}>
            <defs>
              <linearGradient id={`trending-arrow-down-`}>
                <stop offset="-3%" style={{ stopColor: "#944EDC" }} />
                <stop offset="100%" style={{ stopColor: "#EF647B" }} />
              </linearGradient>
            </defs>
          </TrendingDownArrow>
        </Box>
        <Typography variant="subtitle2">Down</Typography>
      </div>
    );
  }

  return null;
}
