interface DepartmentAttributes {
  clientId: string;
  dname: string;
  isActive: boolean;
  isParent: string;
  isRoot: string;
  kpis: any;
  storeId: string;
}

export interface Department {
  created_at: string;
  uoneId: string;
  entityId: string;
  clientId: string;
  type: string;
  subType: string;
  attributes: DepartmentAttributes;
  mask?: DepartmentAttributes;
}

export interface DeptNameEditIProps {
  department: Department;
}

export default DeptNameEditIProps;
