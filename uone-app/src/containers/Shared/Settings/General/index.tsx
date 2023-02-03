import React, { memo } from "react";
import Grid from "@material-ui/core/Grid";
import { useForm } from "react-hook-form";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import useSWR from "swr";

import useStyle from "./styles";

import {
  LANGUAGE_OPTIONS,
  TIMEZONE_OPTIONS,
  RECURRENCE_OPTIONS,
  DEFAULT_ROLES,
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../constants";

type GeneralInputs = {
  language: string;
  timezone: string;
  points: number;
  budgetAmount: number;
  budgetRecurrence: string;
};

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

const budgetApiPayload = {
  path: "/setting/settings/getBudget",
  method: "POST",
};

const raasProvidersApiPayload = {
  path: "/raas/admins/raas-providers",
  method: "GET",
};

const General = () => {
  const styles = useStyle();
  const { enqueueSnackbar } = useSnackbar();
  const { register } = useForm<GeneralInputs>();

  const { data: providersData } = useSWR(
    [raasProvidersApiPayload.path, raasProvidersApiPayload],
    {
      suspense: false,
    }
  );

  const { data: budgetData } = useSWR(
    [budgetApiPayload.path, budgetApiPayload],
    {
      suspense: false,
    }
  );

  const points = parseFloat(
    providersData && providersData.length ? providersData[0]._conversionRate : 1
  );
  const budgetAmount = parseFloat(
    providersData && providersData.length
      ? providersData[0]._maxPointsPerDay
      : 1
  );
  const budgetRecurrence = budgetData?.response?.split("|")[1] || "DAILY";

  const [settings, setSettings] = React.useState<GeneralInputs>({
    language: "",
    timezone: "",
    points,
    budgetAmount,
    budgetRecurrence,
  });

  React.useEffect(() => {
    setSettings((state) => ({
      ...state,
      points,
      budgetAmount,
      budgetRecurrence,
    }));
  }, [providersData, budgetData, points, budgetAmount, budgetRecurrence]);

  const handlePointsChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const value = event.target.value;
    setSettings({
      ...settings,
      points: parseFloat(value),
    });

    debounce(
      API.put(
        apiName,
        `/raas/admins/raas-providers/${
          providersData && providersData.length ? providersData[0]._id : 1
        }/conversion-rate`,
        {
          body: {
            conversionRate: value,
          },
        }
      )
        .then(() => {
          enqueueSnackbar("Points set successfully", SUCCESS_TOAST_OPTIONS);
        })
        .catch(() => {
          enqueueSnackbar("Failed to update points", ERROR_TOAST_OPTIONS);
        }),
      2000
    );
  };

  const handleBudgetAmountChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const value = event.target.value;
    setSettings({
      ...settings,
      budgetAmount: parseFloat(value),
    });

    debounce(
      API.put(
        apiName,
        `/raas/admins/raas-providers/${
          providersData && providersData.length ? providersData[0]._id : 1
        }/max-points-per-day`,
        {
          body: {
            maxPointsPerDay: value,
          },
        }
      )
        .then(() => {
          enqueueSnackbar("Budget set successfully", SUCCESS_TOAST_OPTIONS);
        })
        .catch(() => {
          enqueueSnackbar("Failed to update budget", ERROR_TOAST_OPTIONS);
        }),
      2000
    );
  };

  const handleBudgetRecurrenceChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    const value = event.target.value;
    setSettings({
      ...settings,
      budgetRecurrence: value,
    });

    enqueueSnackbar("Budget set successfully", SUCCESS_TOAST_OPTIONS);
  };

  const handleLanguageChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    const value = event.target.value;

    setSettings({
      ...settings,
      language: value,
    });

    enqueueSnackbar("Language set successfully", ERROR_TOAST_OPTIONS);
  };

  const handleTimezoneChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    const value = event.target.value;

    setSettings({
      ...settings,
      timezone: value,
    });

    enqueueSnackbar("Timezone set successfully", SUCCESS_TOAST_OPTIONS);
  };

  const Divider = () => <div className={styles.divider} />;

  React.useEffect(() => {
    if (providersData && budgetData) {
    }
  });

  return (
    <>
      <Grid container direction="row" className={styles.container}>
        <form className={styles.form}>
          {/* Language input */}
          <label htmlFor="language" className={styles.formLabel}>
            Language
          </label>
          <select
            name="language"
            defaultValue="EN_US"
            className={styles.formSelect}
            ref={register}
            onChange={handleLanguageChange}
          >
            {LANGUAGE_OPTIONS.map((o, i) => (
              <option value={o.value} key={i}>
                {o.label}
              </option>
            ))}
          </select>
          <Divider />

          {/* Timezone input */}
          <label htmlFor="timezone" className={styles.formLabel}>
            Timezone
          </label>
          <select
            name="timezone"
            defaultValue="America/Denver"
            className={styles.formSelect}
            ref={register}
            disabled={true}
            onChange={handleTimezoneChange}
          >
            {TIMEZONE_OPTIONS.map((o, i) => (
              <option value={o.tzCode} key={i}>
                {o.label}
              </option>
            ))}
          </select>
          <Divider />

          {/* Points */}
          <label htmlFor="points" className={styles.formLabel}>
            Points
          </label>
          <div className={styles.pointsInputContainer}>
            <span>1 dollar =</span>
            <input
              name="points"
              placeholder="Enter dollar amount"
              type="number"
              className={styles.pointsInput}
              onChange={handlePointsChange}
              value={settings.points}
            />
            <span>coins</span>
          </div>

          <br />

          {/* Budget Amount & Recurrence */}
          <label htmlFor="budgetAmount" className={styles.formLabel}>
            Budget
          </label>
          <span>
            Points are distributed evenly to teams based on number of agents.
          </span>
          <div className={styles.budgetInputContainer}>
            <input
              name="budgetAmount"
              placeholder="Enter dollar amount"
              type="number"
              className={styles.budgetAmountInput}
              onChange={handleBudgetAmountChange}
              value={settings.budgetAmount}
            />
            <select
              name="budgetRecurrence"
              value={settings.budgetRecurrence}
              className={styles.budgetRecurrenceInput}
              ref={register}
              onChange={handleBudgetRecurrenceChange}
            >
              {RECURRENCE_OPTIONS.map((o, i) => (
                <option value={o.value} key={i}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
        </form>
      </Grid>
    </>
  );
};

export default memo(General);
