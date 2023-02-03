import React from "react";

interface PerformanceCardHeaderProps {
  children: React.ReactNode;
  status: string;
  statusColor: string;
  mainText: string;
  secondaryText: string;
  titlesCss?: any;
  deptText?: string;
}

export default PerformanceCardHeaderProps;
