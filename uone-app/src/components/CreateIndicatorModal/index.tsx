import React, { useState } from "react";
import { Dialog, Grid, Typography, Box } from "@material-ui/core";
import { RHFInputComponent, ButtonSubmit } from "components";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import { useRecoilValue } from "recoil";
import { yupResolver } from "@hookform/resolvers/yup";
import { useForm } from "react-hook-form";

import config from "../../config";
import IndicatorFormulaCreator from "components/CreateIndicatorModal/IndicatorFormulaCreator";
import { UOneDialogContent, UOneDialogTitle } from "components/UOneDialog";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
import { userAtom } from "state";
import { parseNodeTree, getExpressionTree } from "./expresionTreeParser";
import {
  defaultValuesCreateIndicator,
  validationSchemaCreateIndicator,
} from "./hookFormDataCreateIndicator";
import useCreateIndicatorModalStyle from "./style";
import CreateIndicatorModalProps from "./types";
import RHFIOSSwitchComponent from "components/RHFSwitchIOS";

const CreateIndicatorModal: React.FC<CreateIndicatorModalProps> = ({
  open,
  onClose,
  callback,
  departmentId,
}) => {
  const classes = useCreateIndicatorModalStyle();
  const { enqueueSnackbar } = useSnackbar();
  const { clientId } = useRecoilValue(userAtom);
  const [lockButton, setLockButton] = useState(false);
  const [indicatorName, setIndicatorName] = useState("");
  const [flip, setFlip] = useState(false);

  const [formula, setFormula] = useState("");

  const { control, handleSubmit, errors, reset, setValue } = useForm({
    resolver: yupResolver(validationSchemaCreateIndicator),
    ...defaultValuesCreateIndicator,
  });

  const createIndicator = async (values: any) => {
    if (lockButton) return false;

    setLockButton(true);

    const node = getExpressionTree(formula);

    const indicatorExpression = parseNodeTree(node);

    const newKpi = {
      type: "kpi",
      departmentId: `${departmentId}`,
      groupId: `${departmentId}`,
      clientId: `${clientId}`,
      attributes: {
        name: indicatorName,
        expression: indicatorExpression,
        clientId: `${clientId}`,
        type: "kpi",
        departmentId: `${departmentId}`,
        groupId: `${departmentId}`,
        expressionString: formula,
        flip: flip,
      },
    };

    await API.post(config.apiGateway.NAME, `/entity`, { body: newKpi })
      .then(() => {
        callback();
        cleanForm();
        onClose();
        enqueueSnackbar(
          "Indicator created successfully",
          SUCCESS_TOAST_OPTIONS
        );
      })
      .catch(() => {
        enqueueSnackbar("Failed to create indicator", ERROR_TOAST_OPTIONS);
      });
    setLockButton(false);
  };

  const cleanForm = () => {
    setIndicatorName("");
    setFormula("");
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"md"}>
      <UOneDialogTitle id="coin-store-create-item" onClose={onClose}>
        New Custom Indicator
      </UOneDialogTitle>
      <UOneDialogContent dividers>
        <form onSubmit={handleSubmit(createIndicator)} autoComplete="off">
          <Box className={classes.popupContainer}>
            <Grid container>
              <Grid item xs={6}>
                <RHFInputComponent
                  control={control}
                  name="indicator_name"
                  inputProps={{
                    onChange: (e: any) => setIndicatorName(e.target.value),
                  }}
                  defaultValue={indicatorName}
                  variant="outlined"
                  label="Name"
                  errors={errors}
                />
              </Grid>
              <Grid container>
                <Grid item xs={12}>
                  <IndicatorFormulaCreator
                    setFormula={setFormula}
                    control={control}
                    errors={errors}
                    setValue={setValue}
                  />
                </Grid>
              </Grid>
              <Grid container>
                <Grid item xs={12}>
                  <Typography component="span" className={classes.flipText}>
                    Flip
                  </Typography>
                  <RHFIOSSwitchComponent
                    disabled={false}
                    name="disable-status"
                    value={flip}
                    onChange={() => setFlip(!flip)}
                  />
                </Grid>
              </Grid>
              <Grid container>
                <Grid item xs={12}>
                  <Box className={classes.editButtonSection}>
                    <Typography
                      component="span"
                      className={classes.cancelButton}
                      onClick={onClose}
                    >
                      Cancel
                    </Typography>
                    <ButtonSubmit>Create</ButtonSubmit>
                  </Box>
                </Grid>
              </Grid>
            </Grid>
          </Box>
        </form>
      </UOneDialogContent>
    </Dialog>
  );
};

export default CreateIndicatorModal;
