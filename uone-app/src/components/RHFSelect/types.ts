interface IProps {
  control: any;
  name: string;
  defaultValue: any;
  placeholder?: string;
  label?: string;
  options: any;
  errors?: any;
  style?: any;
  hasMore?:boolean;
  loadMore?:any;
  lastKey?:string
}

export default IProps;
