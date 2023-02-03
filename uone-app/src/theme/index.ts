import { createTheme, ThemeOptions } from "@material-ui/core/styles";
import variants from "./variants";
import typography from "./typography";
import overrides from "./overrides";
import breakpoints from "./breakpoints";
import props from "./props";
import shadows from "./shadows";

const createUOneTheme = (name: string) => {
  let themeConfig = variants.find((variant) => variant.name === name);

  if (!themeConfig) {
    console.warn(new Error(`The theme ${name} is not valid.`));
    themeConfig = variants[0] as ThemeOptions;
  }

  return createTheme({
    spacing: 8,
    breakpoints,
    overrides,
    props,
    typography,
    shadows,
    ...themeConfig,
  });
};

const defaultTheme = createUOneTheme("DEFAULT");

if (process.env.NODE_ENV === "development") {
  console.log("\n=== MUI - theme ===\n", defaultTheme);
}

export default defaultTheme;
