import React from "react";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  IconButton,
} from "@material-ui/core";
import { ImageUpload } from "components";

import styles, { useUploadImageStyles } from "./styles";
import { FaTimes } from "react-icons/fa";
import { FC } from "react";
import { UploadImageDialogProps } from "./types";

const UploadImageDialog: FC<UploadImageDialogProps> = ({
  open,
  onClose,
  title,
  type,
  handleLogoChange,
}: any) => {
  const classes = styles();
  const uploadImageStyles = useUploadImageStyles();

  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="upload-dialog"
      classes={classes}
    >
      {title && (
        <div className={uploadImageStyles.uploadImageDialogHeader}>
          <DialogTitle id="max-width-dialog-title">{title}</DialogTitle>
          <IconButton
            aria-label="close"
            disableRipple
            disableFocusRipple
            size="small"
            color="primary"
            onClick={onClose}
          >
            <FaTimes />
          </IconButton>
        </div>
      )}
      <Divider />
      <DialogContent>
        <ImageUpload
          callback={onClose}
          type={type}
          handleLogoChange={handleLogoChange}
        />
      </DialogContent>
    </Dialog>
  );
};

export default UploadImageDialog;
