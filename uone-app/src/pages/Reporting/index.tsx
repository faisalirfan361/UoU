import React, { useState, useEffect } from "react";
import { API } from "aws-amplify";
import config from "../../config";
import { Loading } from "components";
import { useLayoutContext } from "layouts/LayoutProvider";

const Reporting = () => {
  const { setLayoutTitle } = useLayoutContext();
  const [isReporting, setReporting] = useState("");

  useEffect(() => {
    setLayoutTitle("Reporting");
  }, [setLayoutTitle]);

  useEffect(() => {
    const getReport = async () => {
      if (!isReporting) {
        const reportDashboard = await API.get(
          config.apiGateway.NAME,
          '/quicksight',
          {}
        );

        setReporting(reportDashboard.url);
      }
    };
    getReport();
  }, [isReporting]);

  if (!isReporting) return <Loading isInProgress={true} />;

  return <iframe title="report" src={isReporting} width="100%" height="100%" />;
};

export default Reporting;
