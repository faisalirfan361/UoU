import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";

export interface Kpi {
  kpiId: number;
  kpiCode: string;
  kpiName: string;
  [key: string]: any;
}

interface Response {
  kpis: Kpi[];
}

export const useKpis = (clientId: string): Response => {
  const [kpisList, setKpisList] = useState<Kpi[]>([]);

  const getKpisList = async () => {
    const data = await API.get(
      config.apiGateway.NAME,
      `/kpi/kpis/${clientId}`,
      {}
    );

    setKpisList(data.response);
  };

  useEffect(() => {
    getKpisList();
  }, []);

  const hookValues = useMemo(() => {
    return {
      kpis: kpisList,
    };
  }, [kpisList]);

  return hookValues;
};
