import React, { useState } from "react";
import FilterModalProps from "./types";
import { Button, Dialog, Grid } from "@material-ui/core";

import { ButtonActionDuel } from "components";
import DepartmentSelector from "components/DepartmentSelector";
import FilterSelector from "components/FilterSelector";
import { useDepartments } from "../../hooks/useDepartments";

import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";

import useFilterModalStyle from "./style";

const FilterModal: React.FC<FilterModalProps> = ({ open, onClose }) => {
  const classes = useFilterModalStyle();
  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();

  const [meetingGoals, setMeetingGoals] = useState("");
  const [points, setpoints] = useState("");
  const [people, setPeople] = useState("");

  const goalList = ["All", "meeting", "not meeting"];
  const peopleList = ["All", "team", "individual"];
  const pointsList = ["All", "highest points", "lowest points"];
  const [buttonStatus, setButtonStatus] = useState(false);

  return (
    <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"xs"}>
      <UOneDialogTitle id="coin-store-create-item" onClose={onClose}>
        Create Challenge Filter
      </UOneDialogTitle>
      <UOneDialogContent dividers>
        <Grid container>
          <DepartmentSelector
            options={departments}
            defaultOption={defaultDepartment}
            onSelect={setSelectedDepartment}
          />
          <FilterSelector
            options={peopleList}
            defaultOption={"All"}
            onSelect={setPeople}
            label="Participants"
          />
          <FilterSelector
            options={goalList}
            defaultOption={"All"}
            onSelect={setMeetingGoals}
            label="Meeting Goals"
          />
          <FilterSelector
            options={pointsList}
            defaultOption={"All"}
            onSelect={setpoints}
            label="Points"
          />
        </Grid>
      </UOneDialogContent>
      <UOneDialogActions>
        <Button
          onClick={onClose}
          color="primary"
          variant="outlined"
          size="small"
        >
          Cancel
        </Button>
        <ButtonActionDuel handleOnClick={onClose} disabled={buttonStatus}>
          Apply
        </ButtonActionDuel>
      </UOneDialogActions>
    </Dialog>
  );
};

export default FilterModal;
