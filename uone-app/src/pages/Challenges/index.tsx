import { useLayoutContext } from "layouts/LayoutProvider";
import { useState, useEffect, useContext } from "react";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import { Grid } from "@material-ui/core";

import MainHeader from "components/MainHeader";
import { Can } from "context/Ability/Can";
import ListChallenges from "../../containers/Challenge/ListChallenges";
import Duel from "../../containers/Challenge/DuelContainer";
import { AbilityContext } from "../../context/Ability/Can";
import useStyle from "./style";

interface Tab {
  label: string;
  isVisible: boolean;
}

const Challenges = () => {
  const { setLayoutTitle } = useLayoutContext();
  const [tabIndex, setTabIndex] = useState(0);
  const ability = useContext(AbilityContext);

  const classes = useStyle();

  //ability will figure out which tabs the user has access too
  //Then we will filter the ones that isVisible is false for we dont map them
  const tabs: Tab[] = [
    {
      index: 0,
      label: "Challenges",
      isVisible: ability.can("view", "challenges"),
    },
    {
      index: 1,
      label: "Duel",
      isVisible: ability.can("view", "game-duels"),
    },
  ].filter((tab: any) => tab.isVisible);

  useEffect(() => {
    setLayoutTitle("Games");
  }, [setLayoutTitle]);

  return (
    <>
      <Can I="view" a="Performance HUD">
        <MainHeader overflow="overlay">
          <MainHeader.PerformanceBar showAvatar={true} />
        </MainHeader>
      </Can>

      <Grid container direction="row">
        <Tabs
          value={tabIndex}
          indicatorColor="primary"
          textColor="primary"
          onChange={(_, newValue) => setTabIndex(newValue)}
        >
          {tabs.map((tab: Tab, index: number) => (
            <Tab key={`challenge-page-tab-${index}`} label={tab.label} />
          ))}
        </Tabs>
      </Grid>

      {tabIndex === 0 ? <ListChallenges /> : ""}
      {tabIndex === 1 ? <Duel /> : ""}
    </>
  );
};

export default Challenges;
