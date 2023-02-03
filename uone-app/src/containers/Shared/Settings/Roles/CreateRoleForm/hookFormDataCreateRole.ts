import * as yup from "yup";

export const defaultValues = {
  role: "",
};

export const validationSchema = yup.object().shape(
  {
    role: yup.string().required("Role Name is Required"),
   
  },
);
