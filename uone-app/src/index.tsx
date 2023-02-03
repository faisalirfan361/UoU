import React from "react";
import ReactDOM from "react-dom";
import Amplify from "aws-amplify";
import { RecoilRoot } from "recoil";
import recoilPersist from "recoil-persist";
import { SnackbarProvider } from "notistack";

import { heyDayNowPersistState } from "state/config";

import config from "./config";
import App from "./App";

// https://docs.amplify.aws/start/getting-started/setup/q/integration/react#set-up-frontend
Amplify.configure({
  Auth: {
    mandatorySignIn: true,
    region: config.cognito.REGION,
    userPoolId: config.cognito.USER_POOL_ID,
    identityPoolId: config.cognito.IDENTITY_POOL_ID,
    userPoolWebClientId: config.cognito.APP_CLIENT_ID,
  },
  API: {
    endpoints: [
      {
        name: config.apiGateway.NAME,
        endpoint: config.apiGateway.URL,
        region: config.apiGateway.REGION,
      },
    ],
  },
});

const { RecoilPersist, updateState } = recoilPersist(
  heyDayNowPersistState.atomsToPersist,
  {
    key: heyDayNowPersistState.key,
    storage: localStorage,
  }
);

ReactDOM.render(
  <SnackbarProvider maxSnack={3}>
    <RecoilRoot initializeState={updateState}>
      <RecoilPersist />
      <App />
    </RecoilRoot>
  </SnackbarProvider>,
  document.getElementById("root")
);
