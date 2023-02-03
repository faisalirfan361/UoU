import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";

export interface Metric {
  id: string;
  name: string;
}

interface Response {
  metrics: Metric[];
}

const formatMetricName = (metric: string) => {
  const stringValues = metric.split("_");
  let metricName = "";

  for (let s of stringValues) {
    metricName += ` ${s}`;
  }
  return metricName;
};

export const useMetrics = (clientName: string): Response => {
  const [metricsList, setMetricList] = useState<Metric[]>([]);

  const getMetrics = async () => {
    try {
      const data = await API.post(
        config.apiGateway.NAME,
        `/metric-data/get-metrics`,
        { body: { clientCode: clientName } }
      );

      const dataJSON = JSON.parse(data.data);

      const metrics: Metric[] = dataJSON
        ? dataJSON.map((metric: string) => {
          return {
            id: metric,
            name: formatMetricName(metric),
          };
        })
        : [];

      setMetricList(metrics);
    }
    catch (err) {
      console.log(err);
    }
  };

  useEffect(() => {
    getMetrics();
  }, []);

  const hookValues = useMemo(() => {
    return {
      metrics: metricsList,
    };
  }, [metricsList]);

  return hookValues;
};
