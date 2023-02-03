import { number } from "mathjs";
import * as yup from "yup";

import { ChallengeRecurrentOptions } from "../../../constants";

const today = new Date();
const tomorrow = new Date();
tomorrow.setDate(today.getDate() + 1);

export const recurrentSelectOptions = [
  { value: ChallengeRecurrentOptions.ONCE, label: "Once" },
  { value: ChallengeRecurrentOptions.DAILY, label: "Daily" },
  { value: ChallengeRecurrentOptions.WEEKLY, label: "Weekly" },
  { value: ChallengeRecurrentOptions.MONTHLY, label: "Monthly" },
];

type ChallengeFieldDefaultValue = string | number | null;

export type ChallengeDefaultValuesType = {
  kpi_id?: ChallengeFieldDefaultValue;
  kpi_name?: ChallengeFieldDefaultValue;
  clientId?: ChallengeFieldDefaultValue;
  title?: ChallengeFieldDefaultValue;
  description?: ChallengeFieldDefaultValue;
  winnerPoints?: ChallengeFieldDefaultValue;
  start_date: Date | string;
  end_date: Date | string;
  challengesUser?: string[] | number[] | null;
  schedule: ChallengeRecurrentOptions;
  cstatus?: boolean;
  teams?: string[] | number[] | null;
  author?: ChallengeRecurrentOptions;
};

export const defaultValues: ChallengeDefaultValuesType = {
  kpi_id: null,
  clientId: null,
  title: null,
  description: null,
  winnerPoints: null,
  start_date: today,
  end_date: tomorrow,
  challengesUser: null,
  teams: null,
  schedule: ChallengeRecurrentOptions.ONCE,
  cstatus: true,
};

export const registerValidationSchema = yup.object().shape(
  {
    kpi_id: yup.string().required("Kpi is Required").nullable(),
    clientId: yup.string().required("Client ID Field is Required"),
    title: yup.string().required("Name is Required").nullable(),
    description: yup.string().required("Description is Required").nullable(),
    winnerPoints: yup.string().required("Coins is Required").nullable(),
    start_date: yup
      .date()
      .required("Start Date is Required")
      .default(() => new Date())
      .min(new Date(), "Start date should not be in the past."),
    end_date: yup
      .date()
      .required("End Date is Required")
      .min(
        yup.ref("start_date"),
        "End date can't be before or equal start date"
      ),
    challengesUser: yup
      .array()
      .when("teams", {
        is: null,
        then: yup.array().required("Must select an agent or a team").nullable(),
      })
      .nullable(),
    teams: yup
      .array()
      .when("challengesUser", {
        is: null,
        then: yup.array().required("Must select a team or an agent").nullable(),
      })
      .nullable(),
    schedule: yup.string().required("Schedule is Required"),
  },
  [["challengesUser", "teams"]]
);

export const editValidationSchema = yup.object().shape(
  {
    title: yup.string().required("Name is Required").nullable(),
    description: yup.string().required("Description is Required").nullable(),
    winnerPoints: yup.string().required("Coins is Required").nullable(),
    end_date: yup
      .date()
      .required("End Date is Required")
      .min(
        yup.ref("start_date"),
        "End date can't be before or equal start date"
      ),
    challengesUser: yup
      .array()
      .when("teams", {
        is: null,
        then: yup
          .array()
          .required("Must select an agent or a department")
          .nullable(),
      })
      .nullable(),
    teams: yup
      .array()
      .when("challengesUser", {
        is: null,
        then: yup
          .array()
          .required("Must select a department or an agent")
          .nullable(),
      })
      .nullable(),
    schedule: yup.string().required("Schedule is Required"),
  },
  [["challengesUser", "teams"]]
);
