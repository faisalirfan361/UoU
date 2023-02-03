// @ts-nocheck
import React, { FC, Suspense, memo } from "react";
import { ThemeProvider as MuiThemeProvider } from "@material-ui/core/styles";
import CssBaseline from "@material-ui/core/CssBaseline";
import { createBrowserHistory } from "history";
import { useRecoilValue } from "recoil";
import { Route, Router, Switch } from "react-router-dom";

import { Loading } from "components";
import { appAtom, userAtom } from "state";
import theme from "theme";

import "./App.css";

import ErrorHandler from "context/ErrorHandler";
import publicRoutes from "./routes/public";

const history = createBrowserHistory();

const App: FC = () => {
  /**
   * Because Recoil doesn't see the atoms if it is not in App tree
   * and we are using lazy loading for any route component using atoms.
   *
   * Please put ALL your `statePersist` atoms here!!!
   */
  useRecoilValue(appAtom);
  useRecoilValue(userAtom);

  return (
    <Suspense fallback={<Loading isInProgress={true} />}>
      <CssBaseline />
      <MuiThemeProvider theme={theme}>
        <Router history={history}>
          <ErrorHandler>
            <Switch>
              {publicRoutes.map((route) => (
                <Route key={route.slug} {...route} />
              ))}
            </Switch>
          </ErrorHandler>
        </Router>
      </MuiThemeProvider>
    </Suspense>
  );
};

export default memo(App);
