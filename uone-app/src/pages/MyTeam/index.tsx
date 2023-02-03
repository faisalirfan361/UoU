import { useLayoutContext } from "layouts/LayoutProvider";
import { useEffect } from "react";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";

import useStyle from "./style";
import TeamPerformance from "../../containers/Shared/TeamPerformance";
import MainHeader from "components/MainHeader";

import MyTeamContainer from "../../containers/Shared/Dashboard/MyTeamContainer";
import { Can } from "context/Ability/Can";

const MyTeam = () => {
  const styles = useStyle();
  const { departmentId } = useRecoilValue(userAtom);
  const { setLayoutTitle } = useLayoutContext();

  useEffect(() => {
    setLayoutTitle("My Team");
  }, [setLayoutTitle]);

  return (
    <>
      <Can I="view" a="my-team">
        <MainHeader overflow="overlay">
          <MainHeader.PerformanceBar showAvatar={true} />
        </MainHeader>

        <div className={styles.teamPerformanceSection}>
          <TeamPerformance departmentId={departmentId} />
        </div>
      </Can>
      {/* <Can I="view" a="teams-performance">
        <MyTeamContainer />
      </Can> */}
    </>
  );
};

export default MyTeam;
