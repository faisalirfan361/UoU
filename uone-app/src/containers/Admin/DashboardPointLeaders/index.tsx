import React, { memo, useEffect } from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { useRecoilValue } from "recoil";
import useSWR from "swr";
import _get from "lodash.get";

import { CardSimpleComponent } from "components";

import { userAtom } from "state";

import useStyles from "./style";

let pointLeadersAPIPayload = {
  path: "",
  method: "",
};

const DashboardPointLeadersContainer = (props: any) => {
  // props.showLayoutLoader();

  const classes = useStyles();
  const { clientId } = useRecoilValue(userAtom);

  pointLeadersAPIPayload.path = `/department/departments/getTopLeads/${clientId}`;
  pointLeadersAPIPayload.method = "GET";

  const { data: pointLeadersData } = useSWR(
    [pointLeadersAPIPayload.path, pointLeadersAPIPayload],
    {
      suspense: false,
    }
  );

  useEffect(() => {
    if (pointLeadersData) {
      // props.hideLayoutLoader();
    }
  });

  if (!pointLeadersData) return null;

  const pointLeaders = _get(pointLeadersData, "response", []);

  const listPointLeaders = pointLeaders.map(
    (pointLeader: any, index: string | number) => {
      const leader = {
        singleAvatar: "",
        statusColor: "#EF647B",
        title: _get(pointLeader, "Departments[0].dname", ""),
        subtitle: `${pointLeader.firstName} ${pointLeader.lastName}`,
        points: pointLeader.pointsBalance,
      };

      return (
        <Grid key={index} item xs={12} sm={4}>
          <CardSimpleComponent
            singleAvatar={leader.singleAvatar}
            statusColor={leader.statusColor}
            title={leader.title}
            subtitle={leader.subtitle}
            points={leader.points}
          />
        </Grid>
      );
    }
  );

  return (
    <>
      <Grid className={classes.root} container spacing={2}>
        <Grid item xs={12} sm={6}>
          <Typography
            className={classes.subtitle1}
            gutterBottom
            variant="subtitle1"
          >
            Point Leaders
          </Typography>
        </Grid>
        <Grid item xs={12} sm={6}>
          <Typography
            className={classes.subtitle2}
            gutterBottom
            variant="subtitle2"
          >
            View All Point Leaders
          </Typography>
        </Grid>
      </Grid>
      <Grid container spacing={2}>
        {listPointLeaders}
      </Grid>
    </>
  );
};

export default memo(DashboardPointLeadersContainer);
