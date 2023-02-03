export interface RHFCheckboxProps {
  control: any;
  name: string;
  defaultValue: boolean;
  value: boolean;
  label?: string;
  errors?: any;
  externalOnChange?: (value: any) => void;
  disabled?: boolean;
}
