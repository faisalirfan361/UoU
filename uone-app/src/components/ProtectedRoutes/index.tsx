import { useAbility } from "@casl/react";
import { AbilityContext } from "context/Ability/Can";
import Page404 from "pages/Page404";
import { useEffect } from "react";
import { Redirect, Route, Switch, useLocation } from "react-router-dom";
import protectedRoutes from "../../routes/protected";

export default function ProtectedRoutes() {
  const ability = useAbility(AbilityContext);
  let location = useLocation();

  useEffect(() => {
    (window as any).Appcues.page();
  }, [location]);

  return (
    <Switch>
      {protectedRoutes
        .filter((currentRoute) => ability.can("view", currentRoute.slug))
        .map(({ slug, ...rest }) => (
          <Route key={slug} {...rest} />
        ))}
      <Route path="/" exact>
        {ability.can("view", "my-performance") ? (
          <Redirect to="/my-performance" />
        ) : (
          <Redirect to="/dashboard" />
        )}
      </Route>
      <Route path="*">
        <Page404 />
      </Route>
    </Switch>
  );
}
