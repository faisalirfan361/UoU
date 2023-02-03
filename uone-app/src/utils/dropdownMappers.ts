import { Indicator } from "../hooks/useIndicators";

export interface Kpi {
  kpiId: number;
  kpiCode: string;
  kpiName: string;
  [key: string]: any;
}

export interface Department {
  department_id: string;
  dname: string;
  [key: string]: any;
}

export interface User {
  agentName: string;
  user_id: string;
  [key: string]: any;
}

export interface SelectOption {
  label: string;
  value: string;
}

export interface SelectOptionString {
  label: string;
  value: string;
}

export const mapForIndicators = (kpis: Indicator[]): SelectOptionString[] => {
  return kpis.map((indicator: Indicator) => {
    return {
      label: `${indicator.attributes.name}`,
      value: indicator.entityId,
    };
  });
};

export const mapForKpis = (kpis: Kpi[]): SelectOption[] => {
  return kpis.map((kpi: Kpi) => {
    return {
      label: `${kpi.kpiName} - ${kpi.kpiCode}`,
      value: `${kpi.kpiId}`,
    };
  });
};

export const mapForDepartments = (deps: Department[]): SelectOption[] => {
  if (deps) {
    return deps.map((dep: Department) => {
      return {
        label: dep.dname,
        value: dep.department_id,
      };
    });
  } else {
    return [];
  }
};

export const mapForUsers = (users: User[]): SelectOptionString[] => {
  return users.map((user: User) => {
    return {
      label: user.agentName,
      value: user.user_id,
    };
  });
};
