import React, { useEffect } from "react";
import {
  BarChart,
  Bar,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import Paper from "@material-ui/core/Paper";
import Typography from "@material-ui/core/Typography";
import Box from "@material-ui/core/Box";
import useStyle from "./style";
import useSWR from "swr";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import startOfWeek from "date-fns/startOfWeek";
import startOfMonth from "date-fns/startOfMonth";
import startOfQuarter from "date-fns/startOfQuarter";
import startOfYear from "date-fns/startOfYear";
import format from "date-fns/format";

const kpiAPIPayload = {
  path: "/kpi/kpis/",
  method: "GET",
};

const kpiResultsAPIPayload = {
  path: "/kpi/kpiResults",
  method: "GET",
};

const useKPIs = ({ clientId = "" }) => {
  const { data: { response: dataKPIs = [] } = {} } = useSWR(
    `${kpiAPIPayload.path}${clientId}`,
    {
      suspense: false,
    }
  );

  return dataKPIs ? dataKPIs : [];
};

const useKPIResults = (
  date: string,
  clientId: string,
  departmentId: string
) => {
  const { data: { response: kpiResultsData } = {} } = useSWR(
    `${kpiResultsAPIPayload.path}?client_id=${clientId}&department_id=${departmentId}&rdate=${date}`,
    {
      suspense: false,
    }
  );

  useEffect(() => {
    if (kpiResultsData) {
      // hideLoader();
    }
  });

  return kpiResultsData ? kpiResultsData : [];
};

const getFill = (score: number) => {
  if (score > 80) {
    return "#5AD787";
  } else if (score > 60) {
    return "#FCD248";
  } else {
    return "#EF647B";
  }
};

const getDateFilter = (recurrence: string): string => {
  const now = new Date();
  const dateFormat = "yyyy-MM-dd";

  if (recurrence === "DAILY") {
    return format(now, dateFormat);
  } else if (recurrence === "WEEKLY") {
    return format(startOfWeek(now), dateFormat);
  } else if (recurrence === "MONTHLY") {
    return format(startOfMonth(now), dateFormat);
  } else if (recurrence === "QUARTERLY") {
    return format(startOfQuarter(now), dateFormat);
  }
  return format(startOfYear(now), dateFormat);
};

const getBottomLabel = (recurrence: string): string => {
  const now = new Date();

  if (recurrence === "DAILY") {
    return "Today";
  } else if (recurrence === "WEEKLY") {
    return format(now, "wo") + " Week";
  } else if (recurrence === "MONTHLY") {
    return format(now, "MMMM");
  } else if (recurrence === "QUARTERLY") {
    return format(now, "Qo") + " Quarter";
  }
  return format(now, "yyyy");
};

const MAX_ITEMS = 2500; // TODO: remove this value and allow any number of items

export const PerformanceChart = (props: any) => {
  // props.showLayoutLoader();
  const [tabIndex, setTabIndex] = React.useState(0);
  const [performanceData, setPerformanceData] = React.useState([]);
  const styles = useStyle();

  const { clientId, departmentId } = useRecoilValue(userAtom);
  const kpis = useKPIs({ clientId });
  const dateFilter = getDateFilter(props.recurrence);
  const kpiResults = useKPIResults(dateFilter, clientId, departmentId);

  const [kpi, setKpi] = React.useState(
    Array.isArray(kpis) && kpis.length > 0 ? kpis[0] : null
  );

  React.useEffect(() => {
    if (
      kpis.length > 0 &&
      kpiResults.length > 0 &&
      performanceData.length === 0
    ) {
      const newKpi =
        Array.isArray(kpis) && kpis.length > 0 ? kpis[tabIndex] : null;
      setKpi(newKpi);

      if (newKpi) {
        const newPerformanceData = kpiResults
          .filter((result: any) => {
            return result.kpi_id === newKpi.kpiId;
          })
          .filter((_: any, index: number) => index < MAX_ITEMS)
          .map((result: any) => {
            return {
              [newKpi.kpiCode]: result.score,
              fill: getFill(result.score),
            };
          });

        setPerformanceData(newPerformanceData);
      }
    }
  }, [kpiResults, kpis, tabIndex, performanceData]);

  if (!kpi || kpiResults.length === 0) return <Typography>No data</Typography>;

  const handleTabChange = (_: any, index: number) => {
    setTabIndex(index);
  };

  if (!kpis) return null;

  return (
    <Paper elevation={3} className={styles.paper}>
      <Tabs
        value={tabIndex}
        indicatorColor="primary"
        textColor="primary"
        onChange={handleTabChange}
        className={styles.tabs}
      >
        {kpis.map((kpi: any, index: number) => (
          <Tab label={kpi.kpiCode} key={index} classes={{ root: styles.tab }} />
        ))}
      </Tabs>
      {performanceData.length === 0 && <Typography>No data</Typography>}
      {performanceData.length > 0 && (
        <ResponsiveContainer width="100%" height={400}>
          <BarChart
            width={3000}
            height={400}
            data={performanceData}
            margin={{
              top: 5,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray="5 5" />
            <YAxis tickCount={7} />
            <Tooltip />
            <Bar dataKey={kpi.kpiCode} radius={3} />
          </BarChart>
        </ResponsiveContainer>
      )}

      <Typography>
        <Box textAlign="center" fontWeight="bold" lineHeight={3}>
          {getBottomLabel(props.recurrence)}
        </Box>
      </Typography>
    </Paper>
  );
};

PerformanceChart.defaultProps = {};

export default PerformanceChart;
