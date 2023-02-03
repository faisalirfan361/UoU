import React, { memo } from "react";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import PetsIcon from "@material-ui/icons/Pets";
import Grid from "@material-ui/core/Grid";

import useStyle from "./style";
import TeamsPeople from "containers/Agent/TeamsPeople";
import { TeamPerformance } from "containers";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import config from "config";
import MainHeader from "components/MainHeader";

enum TabIndex {
  PERFORMANCE = 0,
  PEOPLE = 1,
}

const MyTeamContainer = () => {
  const styles = useStyle();
  const [tabIndex, setTabIndex] = React.useState(TabIndex.PERFORMANCE);

  const { departmentId, pointsBalance, avatarImages } =
    useRecoilValue(userAtom);

  const avatarSrc = `${config.targetBucketUrl}${avatarImages?.keys.medium}`;

  const performanceHeaderProps = {
    imageUrl: avatarSrc,
    level: {
      icon: PetsIcon,
      name: "Puppy",
      levelNumber: 1,
    },
    pointsToLevelUp: 0,
    challengesWon: 0,
    points: pointsBalance,
    coins: 0,
  };

  return (
    <>
      <Grid container direction="row" className={styles.tabsContainer}>
        <Tabs
          value={tabIndex}
          indicatorColor="primary"
          textColor="primary"
          onChange={(_, newValue) => setTabIndex(newValue)}
          className={styles.tabs}
        >
          <Tab label="Performance" />
          <Tab label="People" />
        </Tabs>
      </Grid>
      {tabIndex === TabIndex.PERFORMANCE && (
        <div className={styles.teamPerformanceContainer}>
          <TeamPerformance departmentId={departmentId} />
        </div>
      )}
      {tabIndex === TabIndex.PEOPLE && (
        <Grid container direction="row" className={styles.row}>
          <TeamsPeople />
        </Grid>
      )}
    </>
  );
};

export default memo(MyTeamContainer);
