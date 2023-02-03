interface RHFInputMultiProps {
  control: any;
  name: string;
  defaultValue: string | number;
  type?: "number";
  variant?: "standard" | "outlined" | "filled";
  label?: string;
  inputProps?: any;
  errors?: any;
}

export default RHFInputMultiProps;
