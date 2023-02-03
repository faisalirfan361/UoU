export interface IProps {
  control: any;
  name: string;
  defaultValue?: string | number;
  type?: "number";
  variant?: "standard" | "outlined" | "filled";
  label?: string;
  inputProps?: any;
  errors?: any;
  fullWidth?: any;
  rows?: number;
  multiline?: any;
}

export default IProps;
