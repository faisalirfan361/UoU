import { FC } from "react";
import _get from "lodash.get";
import { API } from "aws-amplify";
import { useForm } from "react-hook-form";
import { useRecoilValue } from "recoil";
import { useSnackbar } from "notistack";
import { yupResolver } from "@hookform/resolvers/yup";
import Box from "@material-ui/core/Box";
import Grid from "@material-ui/core/Grid";
import { memo, useEffect, useState, useMemo } from "react";

import config from "config";
import { userAtom } from "state";
import {
  ButtonOutlinedComponent,
  RHFInputComponent,
  RHFSelectComponent,
  RHFDateTimePickerComponent,
  RHFSwitchComponent,
  RHFTreeMultiSelect,
  RHFCheckboxComponent,
  RHFInputMultiComponent,
} from "components";
import {
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../constants";
import { useTeams } from "../../../../hooks/useTeams";
import { useIndicator, Indicator } from "../../../../hooks/useIndicators";
import useCreateChallengeStyle from "./styles";
import { CreateChallengeProps } from "./types";
import {
  defaultValues,
  registerValidationSchema,
  recurrentSelectOptions,
  ChallengeDefaultValuesType,
} from "../hookFormData";
import CreateChallengeSkeleton from "../Skeleton";
import {
  mapForIndicators,
  mapForDepartments,
  mapForUsers,
  SelectOption,
  SelectOptionString,
} from "../../../../utils/dropdownMappers";
import { union } from "lodash";

export type User = {
  agentName: string;
  user_id: string;
  [key: string]: any;
};

const CreateChallengeForm: FC<CreateChallengeProps> = ({ refreshFunction }) => {
  const classes = useCreateChallengeStyle();
  const { enqueueSnackbar } = useSnackbar();
  const { clientId, departmentId, userId } = useRecoilValue(userAtom);
  const [buttonStatus, setButtonStatus] = useState(false);
  const { departments } = useTeams();
  const [usersList, setUsersList] = useState<User[]>([]);
  const [count, setCount] = useState<Number>(0);
  const departmentsForSelect: SelectOption[] = useMemo(
    () => mapForDepartments(departments),
    [departments]
  );
  const { indicatorsData } = useIndicator(clientId);
  const indicatorsForSelect: SelectOptionString[] = useMemo(
    () => mapForIndicators(indicatorsData),
    [indicatorsData]
  );
  const getUsersList = async (lastKey?: string) => {
    let path;
    if (lastKey) {
      path = `/entity/list-all-members?lastKey=${lastKey}`;
    } else {
      path = `/entity/list-all-members`;
    }
    try {
      const result: any = await API.get(config.apiGateway.NAME, path, {});
      const userData = result ? result.data : [];
      const cnt = result ? result.count : 0;
      setUsersList((prev) => prev.concat(userData));
      setCount(cnt);
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };
  useEffect(() => {
    getUsersList();
  }, []);
  const usersForSelect: SelectOptionString[] = useMemo(
    () => mapForUsers(usersList),
    [usersList]
  );

  const { control, handleSubmit, errors, reset, watch, register } = useForm({
    resolver: yupResolver(registerValidationSchema),
    defaultValues,
  });

  /**
   * this fuction is to get user name and department id
   * base on user id
   * @param challengesUser is a array of user id
   * @param challengesTeam is a array of team id
   * @returns
   */
  const usersForTeam = (challengesUser: string[], challengesTeam: string[]) => {
    const result: any = usersList
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
    const kpi_name = indicatorsForSelect.find(
      (ind: any) => ind.value === values.kpi_id
    )?.label;

    const challengesUser: any[] = usersForTeam(
      values.challengesUser,
      values.teams
    );
    // find participants by teams if not Add Members
    if (challengesUser.length < 1) {
      enqueueSnackbar(
        "No Challenge User Selected or No User in Selected Team",
        ERROR_TOAST_OPTIONS
      );
      return;
    }
    const data: ChallengeDefaultValuesType = {
      author: undefined, //is undefined for now
      kpi_id: values.kpi_id,
      kpi_name: kpi_name,
      clientId: values.clientId,
      title: values.title,
      description: values.description,
      winnerPoints: values.winnerPoints,
      start_date: new Date(values.start_date).toISOString(),
      end_date: new Date(values.end_date).toISOString(),
      challengesUser: challengesUser,
      teams: values.teams,
      schedule: values.schedule,
      cstatus: true,
    };
    try {
      await API.post(config.apiGateway.NAME, "/game", {
        body: data,
      });
      enqueueSnackbar("Challenge created successfully", SUCCESS_TOAST_OPTIONS);
      const resetValues = {
        ...defaultValues,
        kpi_id: null,
        challengesUser: null,
        teams: null,
      };

      reset(resetValues);
    } catch (e) {
      enqueueSnackbar("Failed to create challenge", ERROR_TOAST_OPTIONS);
    }

    setButtonStatus(false);
    if (refreshFunction) refreshFunction();
  };

  /**
   *
   * @returns boolean value base on
   * all the values need to create a challenge is available
   */
  const isPageDataReady = () => {
    const agentsReady = usersForSelect && usersForSelect.length > 0;
    const kpisReady = indicatorsForSelect && indicatorsForSelect.length > 0;
    const depsReady = departmentsForSelect && departmentsForSelect.length > 0;

    return agentsReady && kpisReady && depsReady;
  };
  const hasMore = (usersList.length && usersList.length % 100 === 0) as boolean;
  const lastUser = usersList[usersList.length - 1];
  const lastKey = lastUser && lastUser.user_id;
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
                <input
                  type="hidden"
                  name="clientId"
                  value={clientId}
                  ref={register}
                />

                <RHFSelectComponent
                  control={control}
                  name="kpi_id"
                  options={indicatorsForSelect}
                  defaultValue={null} // It can be a number, from the available label and value objects.
                  errors={errors}
                  label="Select Kpi"
                  placeholder="Select Kpi"
                />
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
                errors={errors}
                label="Add Members"
                placeholder="Participants"
                hasMore={hasMore}
                loadMore={() => getUsersList(lastKey)}
              />
            </Grid>
          </Grid>
          <Grid container className={classes.inputContainer}>
            <Grid item xs={12}>
              <RHFSelectComponent
                control={control}
                name="schedule"
                options={recurrentSelectOptions}
                defaultValue={recurrentSelectOptions[0]}
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
                Create Challenge
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

export default memo(CreateChallengeForm);
