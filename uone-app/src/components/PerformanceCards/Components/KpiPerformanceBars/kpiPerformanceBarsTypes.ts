interface KpiSetting{
  goal:number,
  obtained: number,
  [key: string]: any
}

interface KpiPerformanceBarsProps {
  kpiSettings: KpiSetting[]
}

export default KpiPerformanceBarsProps;
