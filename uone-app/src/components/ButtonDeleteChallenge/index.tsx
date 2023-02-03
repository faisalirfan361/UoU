import React, { FC } from "react";

import {
  Box,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from "@material-ui/core";
import { useSnackbar } from "notistack";
import { challengeAtom } from "state";
import { useRecoilState } from "recoil";

import IProps from "./types";

const ButtonDeleteChallenge: FC<IProps> = ({ challenge }) => {
  const [open, setOpen] = React.useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const [challenges, setChallenges] = useRecoilState(challengeAtom);

  const handleDelete = () => {
    setOpen(true);
  };

  const handleConfirm = () => {
    setOpen(false);
    enqueueSnackbar(`${challenge.title} challenge has been deleted.`, {
      variant: "success",
      autoHideDuration: 3000,
    });
    setChallenges(
      challenges.filter(({ cognitoID }) => cognitoID !== challenge?.cognitoID)
    );
  };

  const handleClose = () => {
    setOpen(false);
  };

  return (
    <Box>
      <Chip size="small" label="Delete" onClick={handleDelete} />
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">{"Confirm!"}</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you want to delete this item?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleConfirm} autoFocus>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ButtonDeleteChallenge;
