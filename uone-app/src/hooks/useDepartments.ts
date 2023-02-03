import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";

export type Department = {
  department_id: string;
  dname: string;
  [key: string]: any;
  attributes: any;
  clientId: string;
  entityId: string;
  group_count: number;
  user_count: number;
};

interface RawDepartment {
  entityId: string;
  uoneId?: string;
  attributes: {
    dname: string;
    clientId: string;
  };
  clientId: string;
  group_count: number;
  user_count: number;
  mask: {dname: string} | undefined;
}

type Response = {
  departments: Department[];
  selectedDepartment: Department;
  setSelectedDepartment: (department: Department) => void;
  defaultDepartment: Department;
};

const defaultDepartment = {
  department_id: "-1",
  dname: "Everyone",
  attributes: {},
  clientId: "",
  entityId: "",
  group_count: 0,
  user_count: 0,
};

export const useDepartments = (): Response => {
  const [departmentsList, setDepartmentsList] = useState<Department[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [selectedDepartment, setSelectedDepartment] =
    useState<Department>(defaultDepartment);

  const getDepartmentsList = async () => {
    const data = await API.get(
      config.apiGateway.NAME,
      `/entity/get-entities-by-query`,
      {}
    );
    const deps: Department[] = data
      ? data.map((rawDep: RawDepartment) => {
          return {
            department_id: rawDep.entityId,
            dname: rawDep.mask?.dname ? rawDep.mask?.dname  :rawDep.attributes.dname,
            attributes: rawDep.attributes,
            clientId: rawDep.clientId,
            entityId: rawDep.entityId,
            group_count: rawDep.group_count,
            user_count: rawDep.user_count,
          };
        })
      : [];
    setDepartmentsList(deps);
  };

  useEffect(() => {
    getDepartmentsList();
  }, []);

  const hookValues = useMemo(() => {
    return {
      departments: departmentsList,
      selectedDepartment: selectedDepartment,
      setSelectedDepartment: setSelectedDepartment,
      defaultDepartment: defaultDepartment,
    };
  }, [departmentsList, selectedDepartment, setSelectedDepartment]);

  return hookValues;
};
