import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";

export type Department = {
  department_id: string;
  dname: string;
  [key: string]: any;
};

interface RawDepartment {
  entityId: string;
  uoneId?: string;
  attributes: {
    dname: string;
    clientId: string;
  };
  mask: {
    dname: string;
  } | undefined;
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
};

export const useTeams = (): Response => {
  const [departmentsList, setDepartmentsList] = useState<Department[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [selectedDepartment, setSelectedDepartment] =
    useState<Department>(defaultDepartment);

  const getDepartmentsList = async () => {
    const data = await API.get(
      config.apiGateway.NAME,
      `/entity/list-entities-by-type/group`,
      {}
    );
    const deps: Department[] = data
       ? data.map((rawDep: RawDepartment) => {
          return {
            department_id: rawDep.entityId,
            dname: rawDep.mask?.dname ? rawDep.mask?.dname  :rawDep.attributes.dname,
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