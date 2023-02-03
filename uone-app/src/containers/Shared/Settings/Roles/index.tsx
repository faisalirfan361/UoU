import React, { memo, useMemo, useState } from "react";
import Grid from "@material-ui/core/Grid";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import useSWR from "swr";
import { useRecoilValue } from "recoil";
import CheckCircleRoundedIcon from "@material-ui/icons/CheckCircleRounded";
import HighlightOffRoundedIcon from "@material-ui/icons/HighlightOffRounded";
import { Can } from "context/Ability/Can";
import CreateRoleModal from "./Modal";

import downCaret from "../../../../assets/img/misc/down-caret.png";

import useStyle from "./styles";

import { userAtom } from "state";
import {
  DEFAULT_ROLES,
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../constants";
import { Box, Button } from "@material-ui/core";
import { isEmpty } from "lodash";

const apiName = "ApiGateway";

const debounce = (fn: any, delay: number) => {
  let timer: any = null;
  return function (...args: any[]) {
    // @ts-ignore
    const context = this;
    timer && clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(context, args);
    }, delay);
  };
};

const rolesApiPayload = {
  path: "/entity/list-entities-by-type/module",
  method: "GET",
};

const Roles = () => {
  const styles = useStyle();
  const { enqueueSnackbar } = useSnackbar();
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const [currentRole, setCurrentRole] = React.useState("");
  const [roles, setRoles] = React.useState(DEFAULT_ROLES);
  const { roleId, clientId } = useRecoilValue(userAtom);
  const allRolesApiPayload = {
    path: `/entity/list-all-roles`,
    method: "GET",
  };
  const { data: rolesModuleData } = useSWR(
    [`${rolesApiPayload.path}`, rolesApiPayload],
    {
      suspense: false,
    }
  );
  const { data: allRoles } = useSWR(`${allRolesApiPayload.path}`, {
    suspense: false,
  });

  const [permissionsTable, setPermissionsTable] = React.useState<any[]>([]);
  React.useEffect(() => {
    const mods = rolesModuleData || [];
    const filterRoles =
      allRoles &&
      allRoles.filter(
        (role: any) => role.entityId !== roleId && role.roleName !== "Admin"
      );
    setRoles(filterRoles);
    if (allRoles && rolesModuleData) {
      findRolePermission(filterRoles);
    }
    if (mods && allRoles) {
      const defaultRole = filterRoles.find(
        (role: any) => role.roleName === "Agent"
      );
      if (currentRole === "") {
        setCurrentRole(
          defaultRole ? defaultRole.entityId : filterRoles[0].entityId
        );
      }
    }
    //findRolePermission(mods);
  }, [rolesModuleData, allRoles]);

  React.useEffect(() => {
    if (allRoles && rolesModuleData) {
      findRolePermission(allRoles);
    }
  }, [currentRole]);

  const findRolePermission = (mods: any) => {
    const modulePermissions = mods.map((role: any) => {
      if (role.roleModulePermissions.length > 0) {
        return {
          ...role,
          roleModulePermissions: role.roleModulePermissions.map((perm: any) => {
            return {
              ...perm,
              attributes: rolesModuleData.find(
                (module: any) => module.entityId === perm.module_id
              ).attributes,
            };
          }),
        };
      }
    });
    if (
      Array.isArray(modulePermissions) &&
      modulePermissions.length > 0 &&
      modulePermissions
    ) {
      let moduleRole: any = [];
      for (const role of modulePermissions) {
        if (currentRole === role?.entityId) {
          moduleRole = role;
        }
      }
      moduleRole.roleModulePermissions &&
        setPermissionsTable(
          moduleRole.roleModulePermissions.map((mod: any) => {
            return {
              module_id: mod.module_id,
              title: mod.attributes.title,
              description: mod.attributes.description,
              slug: mod.attributes.slug,
              role: mod,
            };
          })
        );
    }
  };

  const handlePermissionUpdate = (
    value: boolean,
    moduleId: string,
    permission: string
  ) => {
    const newRolesAndPermissions = allRoles.find(
      (role: any) => role.entityId === currentRole
    );
    const newPermissionsArr =
      newRolesAndPermissions.attributes.roleModulePermissions;
    const objIndex = newPermissionsArr.findIndex(
      (obj: any) => obj.module_id === moduleId
    );
    newPermissionsArr[objIndex][permission] = value ? 1 : 0;
    let data = {
      type: "role",
      entityId: currentRole,
      clientId: clientId,
      attributes: {
        roleModulePermissions: newPermissionsArr,
        clientId: newRolesAndPermissions.attributes.clientId,
        roleName: newRolesAndPermissions.roleName,
      },
    };
    debounce(
      API.post(apiName, "/entity/update", {
        body: data,
      })
        .then(() => {
          const permissionsCopy: any[] = permissionsTable.map((perm: any) => {
            if (perm.module_id === moduleId) {
              perm.role[permission] = value ? 1 : 0;
            }
            return perm;
          });

          setPermissionsTable(permissionsCopy);

          enqueueSnackbar("Permission set successfully", SUCCESS_TOAST_OPTIONS);
        })
        .catch(() => {
          enqueueSnackbar("Failed to set permission", ERROR_TOAST_OPTIONS);
        }),
      300
    );
  };

  const openDialog = () => {
    setIsDialogOpen(true);
  };
  const closeDialog = () => {
    setIsDialogOpen(false);
  };

  const handleRoleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    setCurrentRole(value);
  };
  //use permissions of Agent role as minimum permissions for new custom roles
  const minimumModulePermissions = useMemo(() => {
    if (!isEmpty(allRoles)) {
      const mods = allRoles.find((item: any) => item.roleName === "Agent");
      return mods?.attributes.roleModulePermissions;
    }
    return {};
  }, [allRoles]);
  return (
    <>
      <Can I="create" a="coin-store">
        <Box mb={2} className={styles.adminActions}>
          <Button
            variant="contained"
            color="primary"
            onClick={() => {
              openDialog();
            }}
          >
            Create Custom Role
          </Button>
        </Box>
      </Can>
      <Grid container className={styles.container}>
        <Grid container direction="row" className={styles.tableHeaderContainer}>
          <Grid item xs={8}>
            {currentRole && roles && (
              <select
                name="role"
                defaultValue={currentRole}
                value={currentRole}
                className={styles.roleSelect}
                onChange={handleRoleChange}
              >
                {roles.map((o: any, i: number) => (
                  <option value={o.entityId} key={i}>
                    {o.roleName}
                  </option>
                ))}
              </select>
            )}
            <span className={styles.formSelectCaret}>
              <img src={downCaret} alt="down-caret" />
            </span>
          </Grid>
          <Grid item xs={1} className={styles.tableHeader}>
            View
          </Grid>
          <Grid item xs={1} className={styles.tableHeader}>
            Create
          </Grid>
          <Grid item xs={1} className={styles.tableHeader}>
            Edit
          </Grid>
          <Grid item xs={1} className={styles.tableHeader}>
            Delete
          </Grid>
        </Grid>
        {permissionsTable.map((perm: any, index: number) => (
          <Grid
            container
            direction="row"
            className={styles.tableRow}
            style={{ backgroundColor: index % 2 === 0 ? "white" : "inherit" }}
          >
            <Grid item xs={8} className={styles.roleName}>
              {perm.title}
            </Grid>
            {perm.role && perm.role ? (
              <React.Fragment>
                <Grid item xs={1} className={styles.iconContainer}>
                  {perm.role.rview ? (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(false, perm.module_id, "rview")
                      }
                    >
                      <CheckCircleRoundedIcon className={styles.onRoleIcon} />
                    </div>
                  ) : (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(true, perm.module_id, "rview")
                      }
                    >
                      <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                    </div>
                  )}
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  {perm.role.rcreate ? (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(false, perm.module_id, "rcreate")
                      }
                    >
                      <CheckCircleRoundedIcon className={styles.onRoleIcon} />
                    </div>
                  ) : (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(true, perm.module_id, "rcreate")
                      }
                    >
                      <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                    </div>
                  )}
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  {perm.role.redit ? (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(false, perm.module_id, "redit")
                      }
                    >
                      <CheckCircleRoundedIcon className={styles.onRoleIcon} />
                    </div>
                  ) : (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(true, perm.module_id, "redit")
                      }
                    >
                      <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                    </div>
                  )}
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  {perm.role.rdelete ? (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(false, perm.module_id, "rdelete")
                      }
                    >
                      <CheckCircleRoundedIcon className={styles.onRoleIcon} />
                    </div>
                  ) : (
                    <div
                      onClick={() =>
                        handlePermissionUpdate(true, perm.module_id, "rdelete")
                      }
                    >
                      <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                    </div>
                  )}
                </Grid>
              </React.Fragment>
            ) : (
              <React.Fragment>
                <Grid item xs={1} className={styles.iconContainer}>
                  <div
                    onClick={() =>
                      handlePermissionUpdate(true, perm.module_id, "rview")
                    }
                  >
                    <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                  </div>
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  <div
                    onClick={() =>
                      handlePermissionUpdate(true, perm.module_id, "rcreate")
                    }
                  >
                    <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                  </div>
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  <div
                    onClick={() =>
                      handlePermissionUpdate(true, perm.module_id, "redit")
                    }
                  >
                    <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                  </div>
                </Grid>
                <Grid item xs={1} className={styles.iconContainer}>
                  <div
                    onClick={() =>
                      handlePermissionUpdate(true, perm.module_id, "rdelete")
                    }
                  >
                    <HighlightOffRoundedIcon className={styles.offRoleIcon} />
                  </div>
                </Grid>
              </React.Fragment>
            )}
          </Grid>
        ))}
      </Grid>
      {allRoles && (
        <CreateRoleModal
          roleModulePermissions={minimumModulePermissions}
          isOpen={isDialogOpen}
          closeDialog={closeDialog}
          client_id={allRoles[0].clientId}
        />
      )}
    </>
  );
};

export default memo(Roles);
