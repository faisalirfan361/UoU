import React, { useEffect, useState } from "react";
import CreateGoalModalProps from "./types";
import _get from "lodash.get";
import { Dialog } from "@material-ui/core";

import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";

import useCreateNewGoalModalStyle from "./style";
import SliderCard from "containers/GoalsList/SliderCard";

const CreateNewGoalModal: React.FC<CreateGoalModalProps> = ({
  open,
  onClose,
}) => {
  const classes = useCreateNewGoalModalStyle();

  return (
    <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"xl"}>
      <UOneDialogTitle id="create-goal-modal" onClose={onClose}>
        New Goal
      </UOneDialogTitle>
      <UOneDialogContent dividers></UOneDialogContent>
    </Dialog>
  );
};

export default CreateNewGoalModal;
