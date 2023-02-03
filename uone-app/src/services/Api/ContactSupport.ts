import API from "@aws-amplify/api";
import config from "../../config";

const BASE_PATH = "/support";

export const contactSupport = (payload: any): Promise<any> =>
  API.post(config.apiGateway.NAME, `${BASE_PATH}/sendMail`, {
    body: payload,
  });
