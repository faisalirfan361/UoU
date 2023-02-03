import React, { memo, useEffect, useState } from "react";
import { Redirect, useHistory } from "react-router-dom";
import { Auth } from "aws-amplify";
import { useResetRecoilState } from "recoil";
import { appAtom, userAtom } from "state";

import { Loading } from "components";

const Logout = () => {
  const history = useHistory();
  const resetAppAtom = useResetRecoilState(appAtom);
  const resetUserAtom = useResetRecoilState(userAtom);

  const [isLogout, setLogout] = useState(false);
  useEffect(() => {
    resetAppAtom();
    resetUserAtom();

    const logoutEffect = async () => {
      /**
       * https://serverless-stack.com/chapters/redirect-on-login-and-logout.html
       */
      await Auth.signOut().catch((error) =>
        console.log("\n=== Cognito Auth - SignOut ===\n", error)
      );

      localStorage.clear();

      setLogout(true);
      history.push("/");
    };

    !isLogout && logoutEffect();
  }, [isLogout, setLogout]);

  if (!isLogout) {
    return <Loading isInProgress={true} />;
  } else {
    return <Redirect to="/" />;
  }
};

export default memo(Logout);
