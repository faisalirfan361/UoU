import { useEffect } from "react";
import { useRecoilValue } from "recoil";

import { userAtom } from "state";
import { useLayoutContext } from "layouts/LayoutProvider";

import { Grid, Typography } from "@material-ui/core";
import MainHeader from "components/MainHeader";
import { TeamsPerformance } from "containers";
import { Can } from "context/Ability/Can";

const Dashboard = () => {
  const { setLayoutTitle } = useLayoutContext();
  const { roleName, fullName } = useRecoilValue(userAtom);

  useEffect(() => {
    let title = "Dashboard";

    if (roleName.toLowerCase() === "agent") {
      title = "My Performance";
    }

    setLayoutTitle(title);
  }, [setLayoutTitle]);

  return (
    <>
      <Can I="view" a="my-performance">
        <Grid container direction="row">
          <Grid item xs={12}>
            <Typography>{fullName}</Typography>
          </Grid>
        </Grid>
      </Can>

      <MainHeader>
        <MainHeader.HeaderBanner />
        <MainHeader.BadgeAvatar
          {...(roleName.toLowerCase() === "agent" && { position: "left" })}
        />
      </MainHeader>

      <Can I="view" a="teams-performance">
        <TeamsPerformance />
      </Can>
    </>
  );
};

export default Dashboard;
