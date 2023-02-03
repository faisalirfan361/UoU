import { Box } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";

import { BadgeSkeletonStyles } from "./styles";

export default function PerformanceBadgeSkeleton() {
  const classes = BadgeSkeletonStyles();

  return (
    <Skeleton
      animation={false}
      variant="circle"
      width={50}
      height={50}
      classes={classes}
    />
  );
}
