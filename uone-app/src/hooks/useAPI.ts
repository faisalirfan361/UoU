/**
 * https://dev.to/mwarger/aws-amplify-graphql-queries-with-typescript-and-hooks-part-2-custom-hooks-57ho
 */
import { useEffect, useState } from "react";
import { API } from "aws-amplify";

type UseQueryType<ResultType> = {
  loading: boolean;
  error: any;
  data: ResultType;
};

export const useAPI = <ResultType extends {}, VariablesType extends {} = {}>(
  path: string,
  method: string,
  options?: VariablesType
): UseQueryType<ResultType> => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [data, setData] = useState({} as ResultType);

  const fetchQuery = async (
    path: string,
    method: string,
    options?: VariablesType
  ) => {
    try {
      // @ts-ignore
      const response = await API[method]("ApiGateway", path, options);
      setData(response.response);
    } catch (error) {
      console.log("useAPI - error: ", error);
      setError((error as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuery(path, method, options);
  }, []);

  return {
    loading,
    data,
    error,
  };
};

export default useAPI;
