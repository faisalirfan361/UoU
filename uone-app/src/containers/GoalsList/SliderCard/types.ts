export interface IProps {
    index: number;
    kpi_id: string;
    kpi_name: string;
    currentMetric: string;
    currentDuration: number;
    handleMetricChange?: any;
    handleDuraChange?: any;
    currentGoal: number;
    goalMin: number;
    goalMax: number;
    handleFieldChange?: any;
    newGoal?: boolean;
    setNewGoal?(): void;
  }
  
  export default IProps;
  