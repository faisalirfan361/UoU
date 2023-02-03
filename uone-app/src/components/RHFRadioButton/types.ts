export interface RHFRadioOption {
  value: string;
  label: string;
}

export interface RHFRadioButtonProps {
  control: any;
  name: string;
  defaultValue: string;
  placeholder?: string;
  label?: string;
  options: RHFRadioOption[];
  errors?: any;
  externalOnChange?: (value: any) => void;
  row?: boolean;
  className?: string;
}
