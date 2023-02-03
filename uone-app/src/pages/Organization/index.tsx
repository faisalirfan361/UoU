import { useEffect, useState } from "react";

import { Grid, Tab } from "@material-ui/core";
import { TabContext, TabList, TabPanel } from "@material-ui/lab";

import { TeamsTeams, TeamsPeople, TeamsDepartments } from "containers";
import useStyle from "./styles";
import { useLayoutContext } from "layouts/LayoutProvider";

const Organization = () => {
  const { setLayoutTitle } = useLayoutContext();
  const styles = useStyle();
  const [tabIndex, setTabIndex] = useState<string>("0");

  useEffect(() => {
    setLayoutTitle("Organization");
  }, [setLayoutTitle]);

  return (
    <>
      <TabContext value={tabIndex}>
        <Grid container direction="row" className={styles.tabsContainer}>
          <TabList
            indicatorColor="primary"
            textColor="primary"
            onChange={(_, newValue) => setTabIndex(newValue)}
            className={styles.tabs}
          >
            <Tab label="Departments" value="0" />
            <Tab label="Teams" value="1" />
            <Tab label="People" value="2" />
          </TabList>
        </Grid>
        <Grid>
          <TabPanel value="0">
            <TeamsDepartments />
          </TabPanel>
          <TabPanel value="1">
            <TeamsTeams />
          </TabPanel>
          <TabPanel value="2">
            <TeamsPeople />
          </TabPanel>
        </Grid>
      </TabContext>
    </>
  );
};

export default Organization;
