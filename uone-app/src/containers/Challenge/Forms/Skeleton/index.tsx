import { Box } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";

import createChallengeSkeletonStyles from "./styles";

const CreateChallengeSkeleton = () => {
  const classes = createChallengeSkeletonStyles();

  return (
    <Box className={classes.root}>
      <Skeleton variant="text" width={"100%"} height={70} />
      <Skeleton variant="rect" width={"100%"} height={640} />
    </Box>
  );
};

export default CreateChallengeSkeleton;
