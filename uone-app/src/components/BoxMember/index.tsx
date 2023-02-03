import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";

import { BoxUserProfile, BoxKPIMeter, KPIMeter } from "components";

import IProps from "./types";

import useStyles from "./style";
import background from "../../assets/img/bg/blue.jpg";

const BoxMember: FC<IProps> = ({ user, type }) => {
  const classes = useStyles({
    background,
  });

  const { kpis: meKPIS = [] } = user;
  const { KPISettings: teamKPIS = [] } = user;

  const memberKpis = type === "me" ? meKPIS : teamKPIS;

  return (
    <Box className={classes.root}>
      <BoxUserProfile
        user={{
          background: user.background ? user.background : background,
          avatar: user.profileImg,
          username: user.username,
          firstName: user.firstName,
          lastName: user.lastName,
          statusColor: user.statusColor
            ? user.statusColor
            : "rgb(235, 192, 106)",
          badges: user.badges,
        }}
      />
      <BoxKPIMeter>
        {memberKpis.map((kpi: any, index: any) => (
          <KPIMeter
            name={kpi?.KPI?.name}
            key={index}
            label={kpi?.KPI?.code}
            progress={+kpi?.optained / +kpi?.goal}
            goal={1}
            barColor={index & 1 ? "#e37458" : "#f4ab5c"}
          />
        ))}
      </BoxKPIMeter>
    </Box>
  );
};

export default memo(BoxMember);
