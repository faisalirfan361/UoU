import React, { memo } from "react";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import Grid from "@material-ui/core/Grid";
import { useSnackbar } from "notistack";
import { useRecoilValue } from "recoil";
import General from "./General";
import Roles from "./Roles";
import PointsCoinStore from "./PointsCoinStore";

import { userAtom } from "state";

import useStyle from "./styles";

interface Tab {
  label: string;
  component: any;
}

const Settings = () => {
  const classes = useStyle();
  const [tabIndex, setTabIndex] = React.useState(0);

  const tabs: Tab[] = [
    {
      label: "General",
      component: General,
    },
    {
      label: "Coin Store",
      component: PointsCoinStore,
    },
    {
      label: "Roles & Permissions",
      component: Roles,
    },
  ];

  return (
    <>
      <Grid container direction="row" className={classes.tabsContainer}>
        <Tabs
          value={tabIndex}
          indicatorColor="primary"
          textColor="primary"
          onChange={(_, newValue) => setTabIndex(newValue)}
          className={classes.tabs}
        >
          {tabs.map((tab: Tab, index: number) => (
            <Tab key={`setting-page-tab-${index}`} label={tab.label} />
          ))}
        </Tabs>
      </Grid>
      {tabs.map((tab: Tab, index) => (
        <div
          className={classes.tabsContainer}
          hidden={tabIndex !== index}
          id={`tab-content-${index}`}
          key={`setting-page-tab-content-${index}`}
        >
          <tab.component />
        </div>
      ))}
    </>
  );
};

export default memo(Settings);
