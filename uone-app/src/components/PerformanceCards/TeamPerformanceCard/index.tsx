import React, { FC } from "react";
import CardContent from "@material-ui/core/CardContent";
import Divider from "@material-ui/core/Divider";
import Card from "@material-ui/core/Card";

import GroupedAvatarsComponent from "../../GroupedAvatars";
import PerformanceCardHeader from "../Components/PerformanceCardHeader";
import KpiPerformanceBars from "../Components/KpiPerformanceBars";
import CardCTA from "../Components/CardCTA";
import IProps from "./TeamPerformanceCardTypes";
import Style from "./style";
import { getTeamStatus, useStatusColor } from "../../../utils/kpiStatusHelper";
import StyledAvatar from "components/StyledAvatar";

const TeamPerformanceCard: FC<IProps> = ({
  team,
  teamDetailsFunc,
  defaultGoal,
}) => {
  if (!team) {
    return null;
  }

  const { getStatusColor } = useStatusColor();
  const status = team.attributes.kpis && getTeamStatus(team.attributes.kpis);
  const statusColor = getStatusColor(status);

  const overrideProps = { statusColor };
  const classes = Style(overrideProps)();

  /**
   *
   * @param team is each team meta data for team card on Admin Dashboard
   * @returns team goal data
   */
  const getKpiSettings = (attr: any) => {
    if (!attr || !attr.kpis || attr.kpis.length === 0) return defaultGoal;

    const activeArray = defaultGoal.map((goal: any) => {
      return goal.title;
    });

    const activeGoals = attr.kpis.filter((kpi: any) =>
      activeArray.includes(kpi.kpi_name)
    );

    return activeGoals.map((kpi: any) => {
      return {
        code: kpi.kpi_code,
        title: kpi.kpi_name,
        goal: kpi.settings.goal,
        obtained: kpi.settings.optained,
      };
    });
  };

  const kpisSettings = getKpiSettings(team.attributes);
  return (
    <Card classes={{ root: classes.root }}>
      <CardContent classes={{ root: classes.cardContent }}>
        <PerformanceCardHeader
          status={status}
          statusColor={statusColor}
          mainText={`${team.manager_first_name} ${team.manager_last_name}`}
          secondaryText={`TEAM:`}
          deptText={team.mask?.dname ? team.mask?.dname : team.attributes.dname}
        >
          <StyledAvatar src={team.manager_img} className={classes.userAvatar} />
        </PerformanceCardHeader>
        <Divider classes={{ root: classes.cardContentDivider }} />
        {kpisSettings && <KpiPerformanceBars kpiSettings={kpisSettings} />}
        <CardCTA actionFunc={teamDetailsFunc} ctaText={"View Team"} />
      </CardContent>
    </Card>
  );
};

export default TeamPerformanceCard;
