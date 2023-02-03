import { useEffect } from "react";
import { useRecoilValue } from "recoil";

import { userAtom } from "state";
import { useLayoutContext } from "layouts/LayoutProvider";

import { Grid, Typography } from "@material-ui/core";
import MainHeader from "components/MainHeader";
import UserPerformance from "containers/Shared/UserPerformance";
import MyPerformanceHud from "containers/MyPerformanceHud";
import { Can } from "context/Ability/Can";

const MyPerformance = () => {
  const { setLayoutTitle } = useLayoutContext();
  const { fullName, userId, departmentId } = useRecoilValue(userAtom);

  useEffect(() => {
    let title = "My Performance";

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
        <MainHeader.BadgeAvatar {...{ position: "left" }} />

        <Can I="view" a="my-performance">
          <MainHeader.PerformanceBar />
        </Can>
      </MainHeader>

      <Can I="view" a="my-performance">
        <MyPerformanceHud />
      </Can>

      <Can I="view" a="my-performance">
        <UserPerformance
          departmentId={departmentId}
          userId={userId}
          confetti={true}
        />
      </Can>
    </>
  );
};

export default MyPerformance;
