export interface Department {
  department_id: string;
  dname: string;
  [key: string]: any;
  attributes: any;
  clientId: string;
  entityId: string;
  group_count: number;
  user_count: number;
}

export interface DepartmentSelectorProps {
  options: Department[];
  defaultOption: Department;
  onSelect: (department: Department) => void;
}
