import { AnyRecord } from "dns";
import * as yup from "yup";

export const goalDurationOptions = [
  { value: "Daily", label: "Daily" },
  { value: "Weekly", label: "Weekly" },
  { value: "Monthly", label: "Monthly" },
];

export const goalTypeOptions = [
  { value: "Time", label: "Time" },
  { value: "Dollar", label: "Dollar" },
  { value: "Number", label: "Number" },
  { value: "Percent", label: "Percent" },
];

export const defaultValuesCreate = {
  goal_name: "",
  metric_type: "",
  metric_duration: "",
  weight: 0,
  points: 0,
  max_number: 100,
  min_number: 0,
  goal_val: 0,
  min_infinite: false,
  max_infinite: false,
};

export const defaultValuesEdit = {
  metric_type: "",
  metric_duration: "",
  weight: 0,
  points: 0,
  max_number: 100,
  min_number: 0,
  goal_val: 0,
  min_infinite: false,
  max_infinite: false,
};

const goal_name = yup.string().required("Name is required").nullable();
const metric_type = yup.string().required("Type is required").nullable();
const metric_duration = yup
  .string()
  .required("Duration is required")
  .nullable();
const weight = yup
  .number()
  .typeError("Min must be a number")
  .required("Weight is required")
  .integer()
  .min(1, "Min weight is 1")
  .max(1000, "Max weight is 1000")
  .nullable();
const points = yup
  .number()
  .typeError("Min must be a number")
  .required("Points is required")
  .integer()
  .min(1, "Min points is 1")
  .max(1000, "Max points is 1000")
  .nullable();
const min_infinite = yup.boolean().nullable();
const max_infinite = yup.boolean().nullable();
const min_number = yup
  .number()
  .when(["max_infinite", "min_infinite"], {
    is: (max_infinite: boolean, min_infinite: boolean) => {
      return !max_infinite && !min_infinite;
    },
    then: yup
      .number()
      .typeError("Min must be a number")
      .required("Min number is required")
      .lessThan(yup.ref("max_number"), "Has to be less than max"),
    otherwise: yup
      .number()
      .typeError("Min must be a number")
      .required("Min number is required"),
  })
  .nullable();
const max_number = yup
  .number()
  .when(["max_infinite", "min_infinite"], {
    is: (max_infinite: boolean, min_infinite: boolean) => {
      return !max_infinite && !min_infinite;
    },
    then: yup
      .number()
      .typeError("Max must be a number")
      .required("Max number is required")
      .moreThan(yup.ref("min_number"), "Has to be greater than min"),
    otherwise: yup
      .number()
      .typeError("Max must be a number")
      .required("Max number is required"),
  })
  .nullable();
const goal_val = yup
  .number()
  .required("Goal is required")
  .typeError("Goal must be a number")
  .when(["max_infinite"], {
    is: (max_infinite: boolean) => {
      return !max_infinite;
    },
    then: yup.number().max(yup.ref("max_number"), "Can't be greater that max"),
  })
  .when(["min_infinite"], {
    is: (min_infinite: boolean) => {
      return !min_infinite;
    },
    then: yup
      .number()
      .moreThan(yup.ref("min_number"), "Has to be greater than min"),
    otherwise: yup.number().moreThan(0, "Can't be less than 1"),
  });

export const editValidationSchema = yup.object().shape({
  goal_name,
  metric_type,
  metric_duration,
  weight,
  points,
  min_infinite,
  max_infinite,
  min_number,
  max_number,
  goal_val,
});

export const createValidationSchema = yup.object().shape({
  goal_name,
  metric_type,
  metric_duration,
  weight,
  points,
  min_infinite,
  max_infinite,
  min_number,
  max_number,
  goal_val,
});
