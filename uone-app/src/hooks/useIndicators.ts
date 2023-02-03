import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";

export interface Indicator {
  departmentId: string;
  entityId: string;
  clientId: string;
  attributes: any;
  type: string;
  [key: string]: any;
}

interface Response {
  indicatorsData: Indicator[];
  forceRequest: () => void;
}

export const useIndicator = (clientId: string): Response => {
  const [indicatorsList, setIndicatorsList] = useState<Indicator[]>([]);

  const getIndicators = async () => {
    const data = await API.get(config.apiGateway.NAME, `/entity/get-kpis`, {});
    setIndicatorsList(data);
  };

  useEffect(() => {
    getIndicators();
  }, []);

  const forceRequest = () => {
    getIndicators();
  };

  const hookValues = useMemo(() => {
    return {
      indicatorsData: indicatorsList,
      forceRequest,
    };
  }, [indicatorsList, forceRequest]);

  return hookValues;
};
