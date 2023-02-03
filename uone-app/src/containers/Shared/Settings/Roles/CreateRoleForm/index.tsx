import { FC } from "react";
import _get from "lodash.get";
import { API } from "aws-amplify";
import { useForm } from "react-hook-form";
import { useRecoilValue } from "recoil";
import { useSnackbar } from "notistack";
import { yupResolver } from "@hookform/resolvers/yup";
import Grid from "@material-ui/core/Grid";
import { memo, useEffect, useState } from "react";

import config from "config";
import { userAtom } from "state";
import { ButtonOutlinedComponent, RHFInputComponent } from "components";
import {
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../../constants";
import useCreateRoleStyle from "./style";
import { CreateRoleProps } from "./types";
import { defaultValues, validationSchema } from "./hookFormDataCreateRole";
import { Button } from "@material-ui/core";

const CreateRoleForm: FC<CreateRoleProps> = ({
  refreshFunction,
  roleModulePermissions,
  client_id,
}) => {
  const classes = useCreateRoleStyle();
  const { enqueueSnackbar } = useSnackbar();
  const { clientId } = useRecoilValue(userAtom);
  const [buttonStatus, setButtonStatus] = useState(false);

  const { control, handleSubmit, errors, reset } = useForm({
    resolver: yupResolver(validationSchema),
    defaultValues,
  });

  // FORM

  const onSubmit = async (values: any) => {
    setButtonStatus(true);
    if (client_id) {
      const data = {
        type: "role",
        clientId: client_id,
        attributes: {
          clientId: client_id,
          roleModulePermissions: roleModulePermissions,
          roleName: values.role,
        },
      };
      try {
        await API.post(config.apiGateway.NAME, "/entity", {
          body: data,
        });
        enqueueSnackbar("Role created successfully", SUCCESS_TOAST_OPTIONS);
      } catch (e) {
        enqueueSnackbar("Failed to create role", ERROR_TOAST_OPTIONS);
      }
    }
    const resetValues = {
      ...defaultValues,
    };

    reset(resetValues);
    setButtonStatus(false);
    if (refreshFunction) refreshFunction();
  };

  useEffect(() => {}, []);

  return (
    <>
      <form className={classes.form}>
        <Grid container className={classes.inputContainer}>
          <Grid item xs={12}>
            <RHFInputComponent
              control={control}
              name="role"
              defaultValue=""
              variant="outlined"
              label="Role Name"
              errors={errors}
            />
          </Grid>
        </Grid>

        <Grid
          container
          direction="row"
          justifyContent="flex-end"
          alignItems="center"
        >
          <Grid item>
            <ButtonOutlinedComponent
              handleOnClick={() => {
                if (refreshFunction) refreshFunction();
              }}
              disabled={buttonStatus}
            >
              Cancel
            </ButtonOutlinedComponent>
            <Button
              variant="contained"
              color="primary"
              onClick={handleSubmit((d) => onSubmit(d))}
            >
              Create
            </Button>
          </Grid>
        </Grid>
      </form>
    </>
  );
};

export default memo(CreateRoleForm);
