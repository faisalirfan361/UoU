export interface IProps {
  control: any;
  name: string;
  step: number;
  max: number;
  defaultValue: number | [number];
  label?: string;
  setChangeCommitted: any;
}

export default IProps;
