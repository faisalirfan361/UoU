import _get from "lodash.get";
import { memo } from "react";
import { Dialog } from "@material-ui/core";

import { UOneDialogTitle, UOneDialogContent } from "components/UOneDialog";
import useCreateRoleModalStyle from "./style";
import CreateRoleModalProps from "./types";
import CreateRoleForm from "../CreateRoleForm";

const CreateRoleModal: React.FC<CreateRoleModalProps> = ({
  isOpen,
  closeDialog,
  roleModulePermissions,
  client_id,
}) => {
  const classes = useCreateRoleModalStyle();

  return (
    <div>
      <Dialog open={isOpen} onClose={closeDialog}>
        <UOneDialogTitle id="coin-store-item-modal" onClose={closeDialog}>
          New Custom Role
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <CreateRoleForm
            client_id={client_id}
            roleModulePermissions={roleModulePermissions}
            refreshFunction={closeDialog}
          />
        </UOneDialogContent>
      </Dialog>
    </div>
  );
};

export default memo(CreateRoleModal);
