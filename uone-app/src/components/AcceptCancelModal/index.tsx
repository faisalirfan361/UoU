import { FC } from "react";
import { Button } from "@material-ui/core";
import { Dialog } from "@material-ui/core";

import {
  UOneDialogTitle,
  UOneDialogContent,
  UOneDialogActions,
} from "components/UOneDialog";
import AcceptCancelModalProps from "./types";
import useAcceptCancelModalStyles from "./style";

const AcceptCancelModal: FC<AcceptCancelModalProps> = ({
  isOpen,
  title,
  text,
  acceptFunc,
  cancelFunc,
  acceptText = "Accept",
  cancelText = "Cancel",
  disabled = false,
}) => {
  const classes = useAcceptCancelModalStyles();

  return (
    <Dialog open={isOpen} onClose={cancelFunc}>
      <UOneDialogTitle id="accept-cancel-form-dialog" onClose={cancelFunc}>
        <div className={classes.container}>{title}</div>
      </UOneDialogTitle>
      <UOneDialogContent dividers>{text}</UOneDialogContent>
      <UOneDialogActions>
        <Button
          onClick={acceptFunc}
          variant="outlined"
          color="primary"
          size="small"
          disabled={disabled}
        >
          {acceptText}
        </Button>
        <Button
          onClick={cancelFunc}
          variant="outlined"
          className={classes.cancelButton}
          color="primary"
          size="small"
          disabled={disabled}
        >
          {cancelText}
        </Button>
      </UOneDialogActions>
    </Dialog>
  );
};

export default AcceptCancelModal;
