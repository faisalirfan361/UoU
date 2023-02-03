import React, { FC, memo } from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";

import TeamPerformance from "../../TeamPerformance";
import TeamPerformanceDialogProps from "./types";
import { IconButton } from "@material-ui/core";
import { FaTimes } from "react-icons/fa";
import useStyles from "./style";

const TeamsPerformanceDialog: FC<TeamPerformanceDialogProps> = ({
  isDialogOpen,
  departmentId,
  closeDialog,
}) => {
  const classes = useStyles();
  if (departmentId === "0") return null;

  return (
    <Dialog
      fullWidth={true}
      maxWidth={"lg"}
      open={isDialogOpen}
      aria-labelledby="max-width-dialog-title"
    >
      <DialogActions className={classes.actions}>
        <IconButton
          className={classes.closeButton}
          aria-label="close"
          disableRipple
          disableFocusRipple
          size="small"
          color="primary"
          onClick={closeDialog}
        >
          <FaTimes />
        </IconButton>
      </DialogActions>
      <DialogContent id="team-permomance-modal-container">
        <TeamPerformance departmentId={departmentId} hideTitle={true} />
      </DialogContent>
    </Dialog>
  );
};

export default memo(TeamsPerformanceDialog);
