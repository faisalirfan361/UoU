import { Auth } from "aws-amplify";
import config from "../config";
import aws4 from "aws4";
import axios from "axios";

export const fetcher = (path: string, payload: object, fetchType?: string) =>
  Auth.currentCredentials()
    .then(async (currentCredentials) => {
      const credentials = Auth.essentialCredentials(currentCredentials);

      const apiGateway = {
        host: config.apiGateway.HOST,
        url: config.apiGateway.URL + path,
      };

      const request = {
        ...apiGateway,
        ...payload,
        service: "execute-api",
        region: config.apiGateway.REGION,
        path,
      };

      let signedRequest = aws4.sign(request, {
        secretAccessKey: credentials["secretAccessKey"],
        accessKeyId: credentials["accessKeyId"],
        sessionToken: credentials["sessionToken"],
      });

      signedRequest.headers!["Content-Type"] = "application/json";

      delete signedRequest.headers!["Host"];
      delete signedRequest.headers!["Content-Length"];

      if (process.env.NODE_ENV === "development") {
        console.log("\n=== SWR - API - signedRequest ===\n", signedRequest);
      }

      if (!fetchType) {
        return fetch(apiGateway.url, signedRequest as any).then((resp) =>
          resp.json()
        );
      } else if (fetchType === "axios") {
        return axios(apiGateway.url, signedRequest as any).then(
          (resp) => resp.data
        );
      }
    })
    .catch((error) => {
      console.log("fetcher - currentCredentials ===>", error);
    });
