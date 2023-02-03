import { AmplifyAuthenticator, AmplifySignIn } from "@aws-amplify/ui-react";
import { AuthState } from "@aws-amplify/ui-components";
import { Auth } from "aws-amplify";
import { SWRConfig } from "swr";
import { fetcher } from "utils/fetcher";
import Viewer from "containers/Viewer";
import { useEffect, useState } from "react";

import LayoutProvider from "layouts/LayoutProvider";
import DashboardLayout from "layouts/Dashboard";
import { NotificationsProvider } from "context/NotificationsContext";
import ProtectedRoutes from "components/ProtectedRoutes";

const Home = () => {
  let [loggedIn, setLoggedIn] = useState(
    localStorage.isLoggedIn ? true : false
  );
  useEffect(() => {
    let isLoggedIn = async () => {
      try {
        await Auth.currentAuthenticatedUser();
        setLoggedIn(true);
      } catch (ex) {
        console.log(ex);
        setLoggedIn(false);
      }
    };
    isLoggedIn();
  }, []);

  let handleAuthStateChange = (nextAuthState: any, authData: any) => {
    if (nextAuthState === AuthState.SignedIn) {
      localStorage.isLoggedIn = true;
      setLoggedIn(true);
    }
  };

  return (
    <>
      {!loggedIn ? (
        <AmplifyAuthenticator handleAuthStateChange={handleAuthStateChange}>
          <AmplifySignIn
            slot="sign-in"
            hideSignUp
            formFields={[
              {
                type: "email",
                label: "Username *",
                placeholder: "Enter your username (case sensitive)",
                inputProps: {
                  required: true,
                  autocomplete: "username",
                },
              },
              {
                type: "password",
                label: "Password *",
                placeholder: "Enter your password",
                inputProps: { required: true, autocomplete: "new-password" },
              },
            ]}
          ></AmplifySignIn>
        </AmplifyAuthenticator>
      ) : (
        <LayoutProvider>
          <SWRConfig
            value={{
              fetcher,
            }}
          >
            <Viewer>
              <NotificationsProvider>
                <DashboardLayout>
                  <ProtectedRoutes />
                </DashboardLayout>
              </NotificationsProvider>
            </Viewer>
          </SWRConfig>
        </LayoutProvider>
      )}
    </>
  );
};

export default Home;
