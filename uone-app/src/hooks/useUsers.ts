import { useEffect, useState, useMemo } from "react";
import { API } from "aws-amplify";
import config from "../config";
import _get from "lodash.get";

export type User = {
  agentName: string;
  user_id: string;
  [key: string]: any;
};

type Response = {
  users: User[];
  count: Number;
};

export const useUsers = (
  clientId?: string,
  departmentId?: string,
  userId?: string,
  lastKey?: string
): Response => {
  const [usersList, setUsersList] = useState<User[]>([]);
  const [count, setCount] = useState<Number>(0);
  const getUsersList = async (lastKey?: string) => {
    let path;
    if (lastKey) {
      path = `/entity/list-all-members?lastKey=${lastKey}`;
    } else {
      path = `/entity/list-all-members`;
    }
    try {
      const result: any = await API.get(config.apiGateway.NAME, path, {});
      const userData = result ? result.data : [];
      const cnt = result ? result.count : 0;
      setUsersList((prev) => prev.concat(userData));
      setCount(cnt);
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };

  useEffect(() => {
    getUsersList();
  }, []);

  const hookValues = useMemo(() => {
    return {
      users: usersList,
      count: count,
    };
  }, [usersList]);

  return hookValues;
};
