interface IProps {
  control: any;
  name: string;
  label?: string;
  defaultValue?: string;
  format: string;
  variant?: "standard" | "outlined" | "filled";
  errors?: any;
  disable?: boolean;
}

export default IProps;
