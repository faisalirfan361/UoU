import { defineAbility } from "@casl/ability";

export const createAbilityFor = (user: {
  modules: any[];
  roleName: string;
}): any =>
  defineAbility((can) => {
    user.modules.forEach((module: any) => {
      if (module.canView) {
        can("view", module.moduleSlug);
      }

      if (module.canCreate) {
        can("create", module.moduleSlug);
      }

      if (module.canEdit) {
        can("edit", module.moduleSlug);
      }

      if (module.canDelete) {
        can("delete", module.moduleSlug);
      }
    });
  });
