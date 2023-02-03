import React, { memo, useEffect, useState } from "react";
import Grid from "@material-ui/core/Grid";
import { API } from "aws-amplify";

import { UserPerformanceProps } from "./types";
import useStyles from "./style";
import config from "../../../config";
import GoalsMeter from "../../../components/GoalsMeter";
import CustSatIcon from "@material-ui/icons/SentimentSatisfiedAlt";
import { useRecoilValue } from "recoil";
import { userAtom } from "../../../state";
import { Goal } from "components/GoalCard/types";

interface KpiSetting {
  imageUrl: string;
  meterTitle: string;
  meterIcon: any;
  meterStatus: string;
  meterGoal: string;
  meterPosition: string;
  [key: string]: any;
}

const mapKpis = (kpiSettingsRawData: any[], imgPath: string): KpiSetting[] => {
  return kpiSettingsRawData
    ? kpiSettingsRawData.map((kpiSetting: any) => {
        const startDate = kpiSetting.startDate
          ? kpiSetting.startDate
          : new Date().setDate(new Date().getDate() - 5);
        const endDate = kpiSetting.endDate
          ? kpiSetting.endDate
          : new Date().setDate(new Date().getDate() + 2);
        return {
          imageUrl: imgPath,
          meterTitle: kpiSetting.attributes.goalName,
          meterIcon: CustSatIcon,
          meterStatus: kpiSetting.status ? "0" : "2", // this will move
          meterGoal: kpiSetting.attributes.goalValue,
          meterCoins: kpiSetting.attributes.points,
          meterPosition: kpiSetting.attributes.position ? "5" : "10", // this will move
          meterKpiId: kpiSetting.attributes.indicator,
          meterDuration: kpiSetting.attributes.metricDuration,
          startDate: startDate,
          endDate: endDate,
          value: kpiSetting.attributes.goalValue,
          flip: kpiSetting.attributes.flip,
        };
      })
    : [];
};
const UserPerformance: React.FC<UserPerformanceProps> = (props) => {
  let intvl: any = null;
  const classes = useStyles();
  const [kpiSettings, setKpiSettings] = useState<KpiSetting[]>([]);
  const { userId, avatarImages } = useRecoilValue(userAtom);
  const [chartData, setChartData] = useState([]);

  const getAvatarImage = (imageUserId: string) => {
    let avatarUrl = `${config.targetBucketUrl}images/${imageUserId}/avatars/current.png`;
    if (userId == imageUserId) {
      avatarUrl = `${config.targetBucketUrl}${avatarImages?.keys.large}`;
    }
    return avatarUrl;
  };

  const imgPath = getAvatarImage(props.userId);

  const getGraph = async () => {
    let path = `/game/performance`;
    const data = await API.post(config.apiGateway.NAME, path, {
      body: { userId: props.userId },
    });
    if (data.statusCode == 200) {
      setChartData(JSON.parse(data.body).data.kpisState);
    } else {
      setChartData([]);
    }
  };

  const getLiveGraph = async () => {
    try {
      if (!intvl) {
        intvl = setInterval(async () => {
          getGraph();
        }, 10000);
      }
    } catch (e) {}
  };

  useEffect(() => {
    getLiveGraph();
    getGoals();
  }, [userId]);

  useEffect(() => {
    getGraph();
  }, [kpiSettings]);

  useEffect(() => {
    return () => {
      clearInterval(intvl);
    };
  }, []);

  const getGoals = async () => {
    let path = `/entity/get-goals?department=${props.departmentId}`;
    const data = await API.get(config.apiGateway.NAME, path, {});
    const activeGoals = data.filter((goal: Goal) =>
      typeof goal.attributes.status === "undefined"
        ? true
        : goal.attributes.status
    );
    setKpiSettings(mapKpis(activeGoals, imgPath));
  };

  return (
    <div className={classes.root}>
      <Grid container direction="row">
        {kpiSettings.map((meter: any, index: number) => (
          <Grid
            item
            xs={12}
            sm={12}
            md={6}
            lg={6}
            key={`meter-${index}`}
            id={`user-performance-card-${index}`}
          >
            <GoalsMeter
              confetti={props.confetti}
              {...meter}
              meterValue={chartData}
            />
          </Grid>
        ))}
      </Grid>
    </div>
  );
};

export default memo(UserPerformance);
