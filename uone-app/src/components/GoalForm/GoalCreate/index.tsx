import {
  Box,
  Grid,
  Typography,
  Tooltip,
  Button,
  Dialog,
} from "@material-ui/core";
import { yupResolver } from "@hookform/resolvers/yup";
import { } from "@material-ui/core";
import { API } from "aws-amplify";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import { useState, FC } from "react";
import { useSnackbar } from "notistack";
import { FaSync } from "react-icons/fa";

import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import {
  RHFInputComponent,
  RHFSelectComponent,
  RHFCheckboxComponent,
} from "components";
import { useForm } from "react-hook-form";
import { Indicator } from "hooks/useIndicators";
import GoalSlider from "components/GoalSlider";
import {
  createValidationSchema,
  defaultValuesEdit,
  goalTypeOptions,
  goalDurationOptions,
} from "../hookFormData";
import { UOneDialogContent, UOneDialogTitle } from "components/UOneDialog";
import GoalCreateProps from "./types";
import useGoalCreateStyles from "./styles";

const GoalCreate: FC<GoalCreateProps> = ({
  indicator,
  open,
  onClose,
  createGoalCallback,
  departmentId,
}) => {
  const classes = useGoalCreateStyles();
  const { enqueueSnackbar } = useSnackbar();
  const { clientId } = useRecoilValue(userAtom);
  const [goalName, setGoalName] = useState("");
  const [lockButton, setLockButton] = useState(false);
  const [goalValue, setGoalValue] = useState(50);
  const [maxNumber, setMaxNumber] = useState(100);
  const [minNumber, setMinNumber] = useState(0);
  const [minInfinite, setMinInfinite] = useState(false);
  const [maxInfinite, setMaxInfinite] = useState(false);
  const [flip, setFlip] = useState(false);

  const { control, handleSubmit, errors, reset, setValue } = useForm({
    resolver: yupResolver(createValidationSchema),
    ...defaultValuesEdit,
  });

  const onSubmit = async (values: any) => {
    setLockButton(true);

    const data = {
      type: "goal",
      clientId: `${clientId}`,
      departmentId: `${departmentId}`,
      groupId: `${departmentId}`,
      attributes: {
        goalName: goalName,
        clientId: `${clientId}`,
        departmentId: `${departmentId}`,
        groupId: `${departmentId}`,
        goalValue: values.goal_val,
        maxNumber: values.max_number,
        minNumber: values.min_number,
        maxInfinite: values.max_infinite,
        minInfinite: values.min_infinite,
        metricType: values.metric_type,
        metricDuration: values.metric_duration,
        weight: values.weight,
        points: values.points,
        flip: flip,
        indicator: indicator?.entityId,
      },
    };

    try {
      await API.post(config.apiGateway.NAME, "/entity", {
        body: data,
      });
      enqueueSnackbar("Goal created successfully", SUCCESS_TOAST_OPTIONS);
      createGoalCallback();
    } catch (e) {
      enqueueSnackbar("Failed to create Goal", ERROR_TOAST_OPTIONS);
    }
    setLockButton(false);
    reset(defaultValuesEdit);
  };

  const setGoalFromSlider = (val: number) => {
    setValue("goal_val", val);
    setGoalValue(val);
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"xl"}>
      <UOneDialogTitle id="create-goal-modal" onClose={onClose}>
        New Goal
      </UOneDialogTitle>
      <UOneDialogContent dividers>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Box className={classes.card}>
            <Grid container direction="row" className={classes.rowSpace}>
              <Grid item xs={6}>
                <RHFInputComponent
                  control={control}
                  name="goal_name"
                  defaultValue=""
                  variant="outlined"
                  inputProps={{
                    onChange: (event: any) => {
                      setGoalName(event.target.value);
                    },
                  }}
                  label="Goal Name"
                  errors={errors}
                />
              </Grid>
            </Grid>
            <Grid
              container
              direction="row"
              spacing={10}
              className={classes.rowSpace}
            >
              <Grid item xs={4}>
                <span className={classes.labelTxt}>kpi</span>
                {indicator ? (
                  <Tooltip
                    classes={{
                      tooltip: classes.tooltip,
                      popper: classes.popper,
                    }}
                    title={indicator.attributes.express}
                    placement="bottom"
                  >
                    <p className={classes.cardTopLabel}>
                      {indicator.attributes.name}
                    </p>
                  </Tooltip>
                ) : null}
              </Grid>
              <Grid item xs={2}>
                <RHFSelectComponent
                  control={control}
                  name="metric_type"
                  options={goalTypeOptions}
                  defaultValue={""}
                  errors={errors}
                  label="Type"
                  placeholder="Select Type"
                />
              </Grid>
              <Grid item xs={2}>
                <RHFSelectComponent
                  control={control}
                  name="metric_duration"
                  options={goalDurationOptions}
                  defaultValue={""}
                  errors={errors}
                  label="Duration"
                  placeholder="Select Duration"
                />
              </Grid>
              <Grid item xs={2}>
                <RHFInputComponent
                  control={control}
                  name="weight"
                  defaultValue=""
                  variant="outlined"
                  label="Weight"
                  type="number"
                  errors={errors}
                />
              </Grid>
              <Grid item xs={2}>
                <RHFInputComponent
                  control={control}
                  name="points"
                  defaultValue="points"
                  variant="outlined"
                  label="Points"
                  type="number"
                  errors={errors}
                />
              </Grid>
            </Grid>

            <Grid
              container
              spacing={10}
              justifyContent="space-between"
              direction={flip ? "row-reverse" : "row"}
            >
              <Grid item xs={2}>
                <RHFInputComponent
                  control={control}
                  name="min_number"
                  inputProps={{
                    disabled: minInfinite,
                    onChange: (event: any) => {
                      setMinNumber(event.target.value);
                    },
                    className: classes.maxMinInput,
                  }}
                  defaultValue={minNumber}
                  variant="outlined"
                  label="Min Number"
                  type="number"
                  errors={errors}
                />
                <RHFCheckboxComponent
                  control={control}
                  name="min_infinite"
                  defaultValue={minInfinite}
                  value={minInfinite}
                  label={"Infinite"}
                  externalOnChange={setMinInfinite}
                  errors={errors}
                />
                <Typography component="p">
                  {minInfinite ? "∞" : minNumber}
                </Typography>
              </Grid>

              <Grid item xs={8}>
                <div className={classes.goalInput}>
                  <RHFInputComponent
                    control={control}
                    name="goal_val"
                    defaultValue={goalValue}
                    variant="outlined"
                    inputProps={{
                      value: goalValue,
                      onChange: (event: any) => {
                        setGoalValue(event.target.value);
                      },
                    }}
                    label="Goal"
                    type="number"
                    errors={errors}
                  />
                </div>
              </Grid>
              <Grid item xs={2}>
                <RHFInputComponent
                  control={control}
                  name="max_number"
                  defaultValue={maxNumber}
                  variant="outlined"
                  inputProps={{
                    disabled: maxInfinite,
                    onChange: (event: any) => {
                      setMaxNumber(event.target.value);
                    },
                    className: classes.maxMinInput,
                  }}
                  label="Max Number"
                  type="number"
                  errors={errors}
                />
                <RHFCheckboxComponent
                  control={control}
                  name="max_infinite"
                  defaultValue={maxInfinite}
                  value={maxInfinite}
                  externalOnChange={setMaxInfinite}
                  label={"Infinite"}
                  errors={errors}
                />
                <Typography component="p" align={"right"}>
                  {maxInfinite ? "∞" : maxNumber}
                </Typography>
              </Grid>
            </Grid>

            <Grid container>
              <Grid item xs={12}>
                <GoalSlider
                  allowChange={true}
                  minNumber={minNumber}
                  maxNumber={maxNumber}
                  goalValue={goalValue}
                  setGoalVal={setGoalFromSlider}
                  minInfinite={minInfinite}
                  maxInfinite={maxInfinite}
                  flipRange={flip}
                />
              </Grid>
            </Grid>
            <Grid
              container
              direction="row"
              className={`${classes.flipWrapper} ${classes.rowSpace}`}
            >
              <Grid item>
                <Box className={classes.flipBox}>
                  <span className={classes.flipTxt}>Flip Range</span>
                  <FaSync
                    className={classes.flipIcon}
                    onClick={() => {
                      setFlip(!flip);
                    }}
                  />
                </Box>
              </Grid>
            </Grid>
            <Grid
              container
              direction="row"
              justifyContent="flex-end"
              className={classes.rowSpace}
            >
              <Grid item xs={12}>
                <Box className={classes.editButtonSection}>
                  <Typography
                    component="span"
                    className={classes.cancelButton}
                    onClick={onClose}
                  >
                    Cancel
                  </Typography>
                  <Button
                    type="submit"
                    className={classes.outlinedPrimary}
                    disabled={lockButton}
                  >
                    Create
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </form>
      </UOneDialogContent>
    </Dialog>
  );
};

export default GoalCreate;
