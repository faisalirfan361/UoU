import { Card, CardContent, Grid } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";

import coinStoreItemClasses from "./coinStoreItemStyles";

export default function CoinStoreItemSkeleton({ item }: any) {
  const classes = coinStoreItemClasses();

  return (
    <Card className={classes.coinStoreItem}>
      <Skeleton variant="rect" width={150} height={90} />
      <CardContent className={classes.coinStoreItemContent}>
        <Grid container direction="column" spacing={1}>
          <Grid item>
            <Skeleton variant="rect" width={250} height={20} />
          </Grid>
          <Grid item>
            <Skeleton variant="rect" width="100%" height={10} />
          </Grid>
          <Grid item>
            <Skeleton variant="rect" width="80%" height={10} />
          </Grid>
        </Grid>
      </CardContent>
      <div className={classes.coinStoreItemAction}>
        <Grid container direction="column" spacing={1}>
          <Grid item>
            <Skeleton variant="rect" width={60} height={30} />
          </Grid>
          <Grid item>
            <Skeleton variant="rect" width={60} height={10} />
          </Grid>
        </Grid>
      </div>
    </Card>
  );
}
