import React, { FC } from "react";
import IProps from "./types";

import {
  Dialog,
  DialogTitle,
  List,
  ListItem,
  ListItemAvatar,
  Avatar,
  ListItemText,
} from "@material-ui/core";

import useStyles from "./style";

const DialogParticipants: FC<IProps> = ({ participants, onClose, open }) => {
  const classes = useStyles();

  const handleClose = () => {
    onClose();
  };

  return (
    <Dialog onClose={handleClose} open={open}>
      <DialogTitle id="simple-dialog-title">List of participants</DialogTitle>
      <List>
        {participants.map(
          ({ cognitoID, avatar, firstName = "", lastName = "" }) => (
            <ListItem key={cognitoID}>
              <ListItemAvatar>
                <Avatar
                  src={avatar}
                  className={classes.avatar}
                ></Avatar>
              </ListItemAvatar>
              <ListItemText primary={`${firstName} ${lastName}`.trim()} />
            </ListItem>
          )
        )}
      </List>
    </Dialog>
  );
};

export default DialogParticipants;
