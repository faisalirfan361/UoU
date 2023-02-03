import { FC } from "react";
import _get from "lodash.get";
import { API } from "aws-amplify";
import { useForm } from "react-hook-form";
import { useRecoilValue } from "recoil";
import { useSnackbar } from "notistack";
import { yupResolver } from "@hookform/resolvers/yup";
import Box from "@material-ui/core/Box";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { memo, useEffect, useState, useMemo } from "react";

import config from "config";
import { userAtom } from "state";
import {
  ButtonOutlinedComponent,
  RHFInputComponent,
  RHFSelectComponent,
  RHFDateTimePickerComponent,
  RHFTreeMultiSelect,
  RHFInputMultiComponent,
} from "components";
import {
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../constants";
import { useDepartments } from "../../../../hooks/useDepartments";
import { useUsers } from "../../../../hooks/useUsers";
import { useIndicator, Indicator } from "../../../../hooks/useIndicators";
import useCreateChallengeStyle from "./styles";
import { EditChallengeProps } from "./types";
import {
  defaultValues,
  editValidationSchema,
  recurrentSelectOptions,
} from "../hookFormData";
import {
  Kpi,
  mapForDepartments,
  mapForUsers,
  SelectOption,
  SelectOptionString,
} from "../../../../utils/dropdownMappers";
import CreateChallengeSkeleton from "../Skeleton";
import _, { union, uniq } from "lodash";

const EditChallengeForm: FC<EditChallengeProps> = ({
  refreshFunction,
  challenge = null,
}) => {
  const classes = useCreateChallengeStyle();
  const { enqueueSnackbar } = useSnackbar();
  const { clientId, departmentId, userId } = useRecoilValue(userAtom);
  const [buttonStatus, setButtonStatus] = useState(false);

  const { indicatorsData } = useIndicator(clientId);

  const kpiName: string = useMemo(() => {
    if (indicatorsData?.length === 0) return "";

    const indicator = indicatorsData.find(
      (ind: Indicator) => ind.entityId == challenge.kpi_id
    );
    return indicator?.attributes.name;
  }, [indicatorsData]);

  const { departments } = useDepartments();
  const departmentsForSelect: SelectOption[] = useMemo(
    () => mapForDepartments(departments),
    [departments]
  );

  const { users } = useUsers(clientId, departmentId, userId);
  const usersForSelect: SelectOptionString[] = useMemo(
    () => mapForUsers(users),
    [users]
  );

  const challengeHasTeams = useMemo(() => {
    return challenge.teams && challenge.teams.length > 0;
  }, [challenge]);

  const challengeHasUsers = useMemo(
    () => challenge.profiles && challenge.profiles.length > 0,
    [challenge]
  );

  const defaultDeps: SelectOption[] = useMemo(() => {
    if (!challengeHasTeams) return [];
    return departmentsForSelect.filter((d: any) => {
      return challenge.teams.includes(d.value);
    });
  }, [departmentsForSelect]);

  const defaultShedule = useMemo(() => {
    if (!challenge.schedule) return null;
    return recurrentSelectOptions.find(
      (option: any) => option.value === challenge.schedule
    );
  }, [recurrentSelectOptions]);

  const defaultUsers: SelectOptionString[] = useMemo(() => {
    if (!challengeHasUsers) {
      return [];
    }
    return usersForSelect.filter((u: any) => {
      return challenge.profiles.find(
        (user: any) =>
          user.entityId === u.value &&
          !challenge.teams?.includes(user.departmentId)
      );
    });
  }, [usersForSelect]);

  const { control, handleSubmit, errors, reset, setValue, watch, register } =
    useForm({
      resolver: yupResolver(editValidationSchema),
      defaultValues,
    });

  const hasToClone = (values: any, challenge: any) => {
    const valuesStartDate = new Date(values.start_date).toISOString();
    const valuesEndDate = new Date(values.end_date).toISOString();
    return (
      challenge.winnerPoints != values.winnerPoints ||
      challenge.start_date != valuesStartDate ||
      challenge.end_date != valuesEndDate ||
      challenge.challengesUser != values.challengesUser ||
      challenge.teams != values.teams ||
      challenge.schedule != values.schedule
    );
  };

  /**
   * this fuction is to get user name and department id
   * base on user id
   * @param challengesUser is a array of user id
   * @param challengesTeam is a array of team id
   * @returns
   */
  const usersForTeam = (challengesUser: string[], challengesTeam: string[]) => {
    const result: any = users
      .filter(
        (user: any) =>
          challengesTeam?.includes(user.teamId) ||
          challengesUser?.includes(user.user_id)
      )
      .map((user: any) => {
        return {
          user_id: user.user_id,
          first_name: user.firstName,
          last_name: user.lastName,
          team_id: user.teamId,
        };
      });
    return result;
  };

  // FORM
  /**
   * this is onsubmit form function
   * @param values this is form values from create new duels
   */
  const onSubmit = async (values: any) => {
    setButtonStatus(true);
    const challengesUser: any[] = usersForTeam(
      values.challengesUser,
      values.teams
    );
    const data = {
      gameId: challenge.gameId,
      kpi_id: challenge.kpi_id,
      kpi_name: kpiName,
      clientId: clientId,
      title: values.title,
      description: values.description,
      winnerPoints: values.winnerPoints,
      start_date: new Date(values.start_date).toISOString(),
      end_date: new Date(values.end_date).toISOString(),
      challengesUser: challengesUser,
      teams: values.teams,
      clone: hasToClone(values, challenge),
      cstatus: true,
      schedule: values.schedule,
    };
    try {
      const updatedChallenge = await API.post(
        config.apiGateway.NAME,
        "/game/update",
        {
          body: data,
        }
      );

      if (refreshFunction) refreshFunction(challenge.gameId, updatedChallenge);
      enqueueSnackbar("Challenge edited successfully", SUCCESS_TOAST_OPTIONS);
      const resetValues = {
        ...defaultValues,
        kpi_id: null,
        challengesUser: null,
        teams: null,
      };

      reset(resetValues);
    } catch (e) {
      enqueueSnackbar("Failed to edit challenge", ERROR_TOAST_OPTIONS);
    }
    setButtonStatus(false);
  };

  /**
   *
   * @returns boolean value base on
   * all the values need to create a challenge is available
   */
  const isPageDataReady = () => {
    const usersReady = usersForSelect && usersForSelect.length > 0;
    const depsReady = departmentsForSelect && departmentsForSelect.length > 0;
    const defaultDepartmentsReady = challengeHasTeams
      ? defaultDeps && defaultDeps.length > 0
      : true;
    const defaultUsersReady = challengeHasUsers
      ? defaultUsers && defaultUsers.length > 0
      : true;

    return (
      usersReady && depsReady && defaultDepartmentsReady && defaultUsersReady
    );
  };

  /**
   * this useEffect used to set
   * default values of the challenge
   */
  useEffect(() => {
    if (isPageDataReady()) {
      setValue("title", challenge.title);
      setValue("description", challenge.description);
      setValue("winnerPoints", challenge.winnerPoints);
      setValue("start_date", new Date(challenge.start_date));
      setValue("end_date", new Date(challenge.end_date));
      setValue("clientId", challenge.clientId);
      setValue("indicator_id", Number(challenge.kpi_id));
      setValue(
        "challengesUser",
        defaultUsers.map((user) => user.value)
      );
      setValue("departments", defaultDeps);
      setValue("schedule", challenge.schedule);
    }
  }, [defaultUsers, defaultDeps]);

  return (
    <>
      {isPageDataReady() ? (
        <form className={classes.form}>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <Box
                width={1}
                className={`${classes.gridHighlight} ${classes.boxSelectHighlight}`}
              >
                <Typography>{kpiName}</Typography>
              </Box>
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFInputComponent
                control={control}
                name="title"
                defaultValue=""
                variant="outlined"
                label="Challenge Name"
                errors={errors}
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={5}>
              <RHFInputComponent
                control={control}
                name="winnerPoints"
                defaultValue=""
                variant="outlined"
                label="Coins"
                type="number"
                errors={errors}
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFInputMultiComponent
                control={control}
                name="description"
                defaultValue=""
                variant="outlined"
                label="Description"
                errors={errors}
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12} md={6}>
              <Box className={classes.gridHighlight}>
                <RHFDateTimePickerComponent
                  control={control}
                  name="start_date"
                  variant="outlined"
                  format="yyyy-MM-dd HH:mm:ss"
                  disable={true}
                  errors={errors}
                  label="Start Date"
                />
              </Box>
            </Grid>
            <Grid item xs={12} md={6}>
              <Box className={classes.gridHighlight}>
                <RHFDateTimePickerComponent
                  control={control}
                  name="end_date"
                  variant="outlined"
                  format="yyyy-MM-dd HH:mm:ss"
                  errors={errors}
                  label="End Date"
                />
              </Box>
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFTreeMultiSelect
                control={control}
                name="teams"
                options={departmentsForSelect}
                defaultValues={defaultDeps}
                errors={errors}
                label="Selected Teams"
                placeholder="Teams"
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFTreeMultiSelect
                control={control}
                name="challengesUser"
                options={usersForSelect}
                defaultValues={defaultUsers}
                errors={errors}
                label="Add Members"
                placeholder="Participants"
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFSelectComponent
                control={control}
                name="schedule"
                options={recurrentSelectOptions}
                defaultValue={defaultShedule}
                errors={errors}
                label="Schedule"
                placeholder="Schedule"
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
                handleOnClick={handleSubmit((d) => onSubmit(d))}
                disabled={buttonStatus}
              >
                Save
              </ButtonOutlinedComponent>
            </Grid>
          </Grid>
        </form>
      ) : (
        <CreateChallengeSkeleton />
      )}
    </>
  );
};

export default memo(EditChallengeForm);
