export interface MetricPosition {
  start: number;
  end: number;
}

interface IndicatorFormulaCreatorProps {
  setFormula(formula: string): void;
  control: any;
  errors: any;
  setValue: any;
}

export default IndicatorFormulaCreatorProps;
