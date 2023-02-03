import { Box, Grid, Typography, Tooltip, Button } from "@material-ui/core";
import { API } from "aws-amplify";
import { useState, FC, useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { FaSync } from "react-icons/fa";
import { yupResolver } from "@hookform/resolvers/yup";
import { useSnackbar } from "notistack";

import { Indicator } from "hooks/useIndicators";
import GoalSlider from "components/GoalSlider";
import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import { Goal } from "components/GoalCard/types";
import {
  RHFInputComponent,
  RHFSelectComponent,
  RHFCheckboxComponent,
} from "components";
import {
  editValidationSchema,
  defaultValuesEdit,
  goalTypeOptions,
  goalDurationOptions,
} from "../hookFormData";
import GoalEditProps from "./types";
import useGoalEditStyles from "./styles";

const goalNeedsToBeClone = (
  values: any,
  goal: Goal,
  flip: boolean
): boolean => {
  return (
    goal.attributes.flip != flip ||
    goal.attributes.metricType != values.metric_type ||
    goal.attributes.metricDuration != values.metric_duration ||
    goal.attributes.weight != values.weight ||
    goal.attributes.points != values.points ||
    goal.attributes.maxNumber != values.max_number ||
    goal.attributes.minNumber != values.min_number ||
    goal.attributes.goalValue != values.goal_val ||
    goal.attributes.minInfinite != values.min_infinite ||
    goal.attributes.maxInfinite != values.max_infinite
  );
};

const GoalEdit: FC<GoalEditProps> = ({ goal, onClose, onRefresh }) => {
  const classes = useGoalEditStyles();
  const { enqueueSnackbar } = useSnackbar();
  const [goalName, setGoalName] = useState(goal.attributes.goalName);
  const [goalValue, setGoalValue] = useState(goal.attributes.goalValue);
  const [maxNumber, setMaxNumber] = useState(goal.attributes.maxNumber);
  const [minNumber, setMinNumber] = useState(goal.attributes.minNumber);
  const [minInfinite, setMinInfinite] = useState(goal.attributes.minInfinite);
  const [maxInfinite, setMaxInfinite] = useState(goal.attributes.maxInfinite);
  const [flip, setFlip] = useState(goal.attributes.flip);
  const [goalKPI, setGoalKPI] = useState<Indicator>();

  const { control, handleSubmit, errors, reset, setValue } = useForm({
    resolver: yupResolver(editValidationSchema),
    ...defaultValuesEdit,
  });

  const onSubmit = async (values: any) => {
    const hasToCloneGoal = goalNeedsToBeClone(values, goal, flip);
    let data = {
      type: "goal",
      ...(!hasToCloneGoal && { created_at: goal.created_at }),
      entityId: goal.entityId,
      clientId: goal.clientId,
      groupId: goal.groupId,
      attributes: {
        goalName: values.goal_name,
        clientId: `${goal.clientId}`,
        departmentId: `${goal.departmentId}`,
        groupId: goal.groupId,
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
        indicator: goalKPI?.entityId,
        clone: hasToCloneGoal,
      },
    };

    if (!hasToCloneGoal) {
      data.created_at = goal.created_at;
    }

    try {
      await API.post(config.apiGateway.NAME, "/entity/update", {
        body: data,
      });
      enqueueSnackbar("Goal edited successfully", SUCCESS_TOAST_OPTIONS);
    } catch (e) {
      enqueueSnackbar("Failed to edit Goal", ERROR_TOAST_OPTIONS);
    }

    reset(defaultValuesEdit);
    onRefresh();
    onClose();
  };

  useEffect(() => {
    setValue("goal_name", goal.attributes.goalName);
    setValue("metric_type", goal.attributes.metricType);
    setValue("metric_duration", goal.attributes.metricDuration);
    setValue("weight", goal.attributes.weight);
    setValue("points", goal.attributes.points);
    setValue("max_number", goal.attributes.maxNumber);
    setValue("min_number", goal.attributes.minNumber);
    setValue("goal_val", goal.attributes.goalValue);
    setValue("min_infinite", goal.attributes.minInfinite);
    setValue("max_infinite", goal.attributes.maxInfinite);
    setValue("flip", goal.attributes.flip);
    getIndicator(goal.attributes.indicator);
  }, []);

  const getIndicator = async (entityId: string) => {
    const response = await API.get(
      config.apiGateway.NAME,
      `/entity/get-kpis?kpi=${entityId}`,
      {}
    );
    if (response && response.length > 0) {
      const indicator: Indicator = response[0];
      setGoalKPI(indicator);
    }
  };

  const setGoalFromSlider = (val: number) => {
    setValue("goal_val", val);
    setGoalValue(val);
  };

  const defaultGoalType = useMemo(() => {
    if (!goal.attributes.metricType) return null;
    return goalTypeOptions.find(
      (option: any) => option.value === goal.attributes.metricType
    );
  }, [goalTypeOptions]);

  const defaultGoalDuration = useMemo(() => {
    if (!goal.attributes.metricDuration) return null;
    return goalDurationOptions.find(
      (option: any) => option.value === goal.attributes.metricDuration
    );
  }, [goalDurationOptions]);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Box className={classes.card}>
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
              <Button type="submit" className={classes.outlinedPrimary}>
                Save
              </Button>
            </Box>
          </Grid>
        </Grid>
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
            {goalKPI ? (
              <Tooltip
                key={`kpi-name-${goalKPI?.entityId}`}
                classes={{
                  tooltip: classes.tooltip,
                  popper: classes.popper,
                }}
                title={
                  goalKPI.attributes.expressionString
                    ? goalKPI.attributes.expressionString
                    : ""
                }
                placement="bottom"
              >
                <p className={classes.cardTopLabel}>
                  {goalKPI.attributes.name}
                </p>
              </Tooltip>
            ) : null}
          </Grid>
          <Grid item xs={2}>
            <RHFSelectComponent
              control={control}
              name="metric_type"
              options={goalTypeOptions}
              defaultValue={defaultGoalType}
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
              defaultValue={defaultGoalDuration}
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
              defaultValue=""
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
              defaultValue={goal.attributes.minNumber}
              variant="outlined"
              label="Min Number"
              type="number"
              errors={errors}
            />
            <RHFCheckboxComponent
              control={control}
              name="min_infinite"
              defaultValue={goal.attributes.minInfinite}
              label={"Infinite"}
              value={minInfinite}
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
              defaultValue={goal.attributes.maxNumber}
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
              defaultValue={goal.attributes.maxInfinite}
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
      </Box>
    </form>
  );
};

export default GoalEdit;
