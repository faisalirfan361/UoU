import { Box, Grid } from "@material-ui/core";
import { Pagination } from "@material-ui/lab";

import PerformanceBadge from "components/PerformanceBadge/PerformanceBadge";
import PerformanceBadgeSkeleton from "components/PerformanceBadge/PerformanceBadgeSkeleton";
import usePagination from "hooks/usePagination";
import PerformanceBadgesDots from "./PerformanceBadgesDots";

import performanceBadgesStyles from "./styles";
import { PerformanceBadgesProps } from "./types";

export default function PerformanceBadges({
  badges,
  minHeight = "auto",
}: PerformanceBadgesProps) {
  const classes = performanceBadgesStyles();
  const { jump, data, currentPage, maxPage } = usePagination(badges, 14);

  const handlePageChange = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    jump(value);
  };

  return (
    <Box minHeight={minHeight}>
      <Grid
        container
        alignItems="center"
        justify="center"
        className={classes.badgesPagination}
      >
        <Grid item>
          <Box className={classes.badgeNumber}>
            {badges.length > 0 && `${badges.length} Badges`}
          </Box>
          <PerformanceBadgesDots
            count={maxPage}
            page={currentPage}
            onChange={handlePageChange}
          />
        </Grid>
      </Grid>

      <Box className={classes.badges}>
        {data.map((item, index) => {
          if (!item)
            return (
              <PerformanceBadgeSkeleton key={`performance-badge-${index}`} />
            );
          return (
            <PerformanceBadge
              key={`${item.type}-${item.color}-${index}`}
              {...item}
            />
          );
        })}
      </Box>
    </Box>
  );
}
