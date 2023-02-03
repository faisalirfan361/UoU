import React, { FC, memo } from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import Divider from "@material-ui/core/Divider";
import DialogContent from "@material-ui/core/DialogContent";
import { FaTimes } from "react-icons/fa";
import { IconButton } from "@material-ui/core";

import UserPerformance from "../../UserPerformance";
import UserPerformanceProps from "./types";
import useStyles from "./style";
import StyledAvatar from "components/StyledAvatar";

const UserPerformanceDialog: FC<UserPerformanceProps> = ({
  isDialogOpen,
  user,
  closeDialog,
}) => {
  const classes = useStyles();
  if (!user) return null;
  return (
    <>
      <Dialog
        fullWidth={true}
        maxWidth={"sm"}
        open={isDialogOpen}
        aria-labelledby="max-width-dialog-title"
      >
        <DialogActions className={classes.actions}>
          <div className={classes.userInfoContainer}>
            <StyledAvatar src={user.profileImg} className={classes.avatar} />
            <span
              className={classes.userName}
            >{`${user.firstName} ${user.lastName}`}</span>
          </div>
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
        <div className={classes.dividerContainer}>
          <Divider className={classes.divider} component={"hr"} />
        </div>
        <DialogContent className={classes.modalGrids}>
          <UserPerformance
            departmentId={user.department.departmentId}
            userId={user.userId}
            confetti={false}
          />
        </DialogContent>
      </Dialog>
    </>
  );
};

export default memo(UserPerformanceDialog);
