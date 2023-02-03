export interface CreateRoleProps {
  refreshFunction?: () => void;
  client_id: string;
  roleModulePermissions: any;
}
