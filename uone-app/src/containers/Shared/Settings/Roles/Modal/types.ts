export default interface CreateRoleModalProps {
  isOpen: boolean;
  roleModulePermissions: {};
  client_id: string;
  closeDialog: () => void;
}
