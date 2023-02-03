import API from "@aws-amplify/api";
import config from "../../config";

const BASE_PATH = "/badges";

export const single = (id: string): Promise<any> =>
  API.get(config.apiGateway.NAME, `${BASE_PATH}/user/${id}`, {});
