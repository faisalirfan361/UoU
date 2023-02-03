export interface SliderCardProps {
  kpi_name: string,
  handleFieldChange?: any,
  newGoal?: boolean,
  setNewGoal?(): void;
  isEdit?: boolean,
  setIsEdit?: any;
  currentMetric: string,
  currentDuration: number,
  currentGoal: number,
}
  
  export default SliderCardProps;
  