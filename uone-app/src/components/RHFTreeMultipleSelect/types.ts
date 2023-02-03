export default interface RHFTreeSelectProps {
  control: any;
  name: string;
  defaultValues?: any[];
  placeholder?: string;
  label?: string;
  options: any;
  errors?: any;
  hasMore?:boolean;
  loadMore?:any;
  lastKey?:string
}
