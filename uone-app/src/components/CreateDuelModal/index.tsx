import React, { useState, useMemo, useEffect } from "react";
import DuelModalProps from "./types";
import { useRecoilValue } from "recoil";
import { useForm } from "react-hook-form";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import useSWR from "swr";
import * as yup from "yup";
import _get from "lodash.get";
import { Button, Dialog, Grid, Typography } from "@material-ui/core";

import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
import { userAtom } from "state";
import { yupResolver } from "@hookform/resolvers/yup";
import { ButtonActionDuel } from "components";
import {
  mapForIndicators,
  SelectOptionString,
} from "../../utils/dropdownMappers";
import { useIndicator } from "../../hooks/useIndicators";

import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";

import {
  RHFInputComponent,
  RHFInputMultiComponent,
  RHFSelectComponent,
} from "components";

import useDuelModalStyle from "./style";
import { useUsers } from "hooks/useUsers";

const defaultValues = {
  kpi_id: "" || null,
  title: "",
  description: "",
  coins: "",
  duration: "",
  agent_id: "" || null,
};
export type User = {
  agentName: string;
  user_id: string;
  [key: string]: any;
};
/**
 * this is an api call function to get list of users
 * @returns array of users
 */

const validationSchema = yup.object().shape({
  kpi_id: yup.string().required("Metric Field is Required").nullable(),
  title: yup.string().required("Name Field is Required"),
  description: yup.string().required("Description Field is Required"),
  coins: yup.string().required("Winner Points Field is Required"),
  duration: yup.string().required("Winner Points Field is Required"),
  agent_id: yup.string().required("The participant is required").nullable(),
});

const CreateDuelModal: React.FC<DuelModalProps> = ({ open, onClose }) => {
  const classes = useDuelModalStyle();
  const [buttonStatus, setButtonStatus] = useState(false);
  const {
    clientId,
    userId,
    roleId,
    departmentId,
    firstName,
    lastName,
    pointsBalance,
  } = useRecoilValue(userAtom);
  const { enqueueSnackbar } = useSnackbar();
  const { users } = useUsers(clientId, departmentId, userId);
  const [agentsData, setAgentsData] = useState<User[]>([]);
  const coins = Number(pointsBalance);

  const getAgents = async (lastKey?: string) => {
    let path;
    if (lastKey) {
      path = `/entity/get-users?lastKey=${lastKey}`;
    } else {
      path = `/entity/get-users`;
    }
    try {
      const result: any = await API.get(config.apiGateway.NAME, path, {});
      const agentsData = result ? result : [];

      setAgentsData((prev) => prev.concat(agentsData));
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };
  useEffect(() => {
    getAgents();
  }, []);
  const agentsAsSelectOptions: SelectOptionString[] = useMemo(() => {
    const members = agentsData
      ? agentsData.filter((member: any) => member.roleId === roleId)
      : [];
    return members.map((agent: any, index: number) => ({
      label: `${agent.attributes.firstName} ${agent.attributes.lastName}`,
      value: agent.entityId,
    }));
  }, [agentsData]);

  const { indicatorsData } = useIndicator(clientId);
  const indicatorsForSelect: SelectOptionString[] = useMemo(
    () => mapForIndicators(indicatorsData),
    [indicatorsData]
  );

  const { control, handleSubmit, errors, reset, watch, register } = useForm({
    resolver: yupResolver(validationSchema),
    defaultValues,
  });

  /**
   *
   * @param challengesUser is an array of users ids
   * @returns is an array of challenge user objects
   */
  const usersForTeam = (challengesUser: string[]) => {
    const result: any = agentsData
      .filter((user: any) => challengesUser.includes(user.entityId))
      .map((user: any) => {
        return {
          user_id: user.entityId,
          first_name: user.attributes.firstName,
          last_name: user.attributes.lastName,
          team_id: user.groupId,
        };
      });
    if (result.length < 2) {
      result.push({
        user_id: userId,
        first_name: firstName,
        last_name: lastName,
        team_id: departmentId,
      });
    }
    return result;
  };

  /**
   *
   * @param challengesUser is user id
   * @returns is coins of this user
   */
  const getOpponentCoins = (challengesUser: string) => {
    const result: any = agentsData.find(
      (user: any) => challengesUser === user.entityId
    );
    return result ? result.attributes.pointsBalance : "0";
  };

  /**
   * this is onsubmit form function
   * @param values this is form values from create new duels
   */
  const onSubmit = async (values: any) => {
    setButtonStatus(true);
    const challengesUser: any[] = usersForTeam([values.agent_id, userId]);
    const opponentCoins = Number(getOpponentCoins(values.agent_id));
    const minutesToAdd = 30 * 60000;
    const hoursToAdd = Number(values.duration) * 3600000;
    const currentDate = new Date();
    const startDate = new Date(currentDate.getTime() + minutesToAdd);
    const endDate = new Date(startDate.getTime() + hoursToAdd);
    const kpi_name = indicatorsForSelect.find(
      (ind: any) => ind.value === values.kpi_id
    )?.label;
    const data = {
      author: undefined, //is undefined for now
      kpi_id: values.kpi_id,
      kpi_name: kpi_name,
      clientId: values.clientId,
      title: values.title,
      description: values.description,
      winnerPoints: values.coins,
      start_date: new Date(startDate).toISOString(),
      end_date: new Date(endDate).toISOString(),
      challengesUser: challengesUser,
      isAccepted: false,
      isDuel: true,
      isDeclined: false,
      user_id: values.user_id,
    };

    if (Number(values.coins) > 50) {
      enqueueSnackbar(
        `Failed to create duel, Maximum coins limit is 50`,
        ERROR_TOAST_OPTIONS
      );
      setButtonStatus(false);
      return;
    }
    if (
      Number(values.coins) > (opponentCoins || 0) ||
      Number(values.coins) > (coins || 0)
    ) {
      enqueueSnackbar(
        `Failed to create duel, You or Your opponent has less amount of coins than ${values.coins}`,
        ERROR_TOAST_OPTIONS
      );
      setButtonStatus(false);
      return;
    }

    try {
      await API.post(config.apiGateway.NAME, "/game", {
        body: data,
      });
      enqueueSnackbar("Duel created successfully", SUCCESS_TOAST_OPTIONS);
      onClose();
    } catch (e) {
      enqueueSnackbar("Failed to create duel", ERROR_TOAST_OPTIONS);
    }

    const resetValues = {
      ...defaultValues,
    };

    reset(resetValues);
    setButtonStatus(false);
    //if (refreshFunction) refreshFunction();
  };

  const hasMore = (agentsData.length &&
    agentsData.length % 100 === 0) as boolean;
  const lastAgent = agentsData[agentsData.length - 1];
  const lastKey = lastAgent && lastAgent.entityId;

  return (
    <form>
      <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"sm"}>
        <UOneDialogTitle id="coin-store-create-item" onClose={onClose}>
          Create Duel
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <form>
            <Grid container>
              <Grid item xs={12} className={classes.inputContainer}>
                <input
                  type="hidden"
                  name="clientId"
                  value={clientId}
                  ref={register}
                />
                <input
                  type="hidden"
                  name="user_id"
                  value={userId}
                  ref={register}
                />
                <RHFSelectComponent
                  control={control}
                  name="kpi_id"
                  options={indicatorsForSelect}
                  defaultValue={""}
                  errors={errors}
                  label="KPI"
                  placeholder="Select KPI"
                />
              </Grid>
              <Grid item xs={12} className={classes.inputContainer}>
                <RHFInputComponent
                  control={control}
                  name="title"
                  defaultValue=""
                  variant="outlined"
                  label="Duel Name"
                  errors={errors}
                />
              </Grid>
              <Grid
                item
                xs={12}
                className={classes.inputContainer}
                style={{ alignItems: "center" }}
              >
                <Grid item xs={12} md={4}>
                  <RHFInputComponent
                    control={control}
                    name="coins"
                    defaultValue=""
                    variant="outlined"
                    label="Coin Wager"
                    type="number"
                    errors={errors}
                  />
                </Grid>

                <Grid item xs={12} md={8}>
                  <Typography className={classes.typoText}>
                    Wagers use your own coins (50 coin max)
                  </Typography>
                </Grid>
              </Grid>
              <Grid item xs={12} className={classes.inputContainer}>
                <RHFInputMultiComponent
                  control={control}
                  name="description"
                  defaultValue=""
                  variant="outlined"
                  label="Message"
                  errors={errors}
                />
              </Grid>
              <Grid item xs={12} className={classes.inputContainer}>
                <Grid item xs={12} md={4}>
                  <RHFInputComponent
                    control={control}
                    name="duration"
                    defaultValue=""
                    variant="outlined"
                    label="duration"
                    type="number"
                    errors={errors}
                  />
                </Grid>
              </Grid>
              <Grid item xs={12} className={classes.inputContainer}>
                <RHFSelectComponent
                  control={control}
                  name="agent_id"
                  options={agentsAsSelectOptions}
                  defaultValue={""}
                  errors={errors}
                  label="Participant"
                  placeholder="Add Participant"
                  hasMore={hasMore}
                  loadMore={() => getAgents(lastKey)}
                />
              </Grid>
            </Grid>
          </form>
        </UOneDialogContent>
        <UOneDialogActions>
          <Button
            onClick={onClose}
            color="primary"
            variant="outlined"
            size="small"
          >
            Cancel
          </Button>
          <ButtonActionDuel
            handleOnClick={handleSubmit((d) => onSubmit(d))}
            disabled={buttonStatus}
          >
            Send Duel Invite
          </ButtonActionDuel>
        </UOneDialogActions>
      </Dialog>
    </form>
  );
};

export default CreateDuelModal;
