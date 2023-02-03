import { TypographyOptions } from "@material-ui/core/styles/createTypography";

const commonHeadingProps = {
  fontWeight: 600,
  lineHeight: 1.25,
};
const typography: TypographyOptions = {
  fontFamily: ["Poppins", "sans-serif"].join(","),
  fontSize: 14,
  fontWeightLight: 300,
  fontWeightRegular: 400,
  fontWeightMedium: 500,
  fontWeightBold: 600,
  h1: {
    ...commonHeadingProps,
    fontSize: "2rem",
  },
  h2: {
    ...commonHeadingProps,
    fontSize: "1.75rem",
  },
  h3: {
    ...commonHeadingProps,
    fontSize: "1.5rem",
  },
  h4: {
    ...commonHeadingProps,
    fontSize: "1.125rem",
  },
  h5: {
    ...commonHeadingProps,
    fontSize: "1.0625rem",
  },
  h6: {
    ...commonHeadingProps,
    fontSize: "1rem",
  },
  body1: {
    fontSize: "1rem",
  },
  button: {
    textTransform: "none",
  },
};

export default typography;
