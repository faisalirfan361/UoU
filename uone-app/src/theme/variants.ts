import { ThemeOptions, PaletteType } from "@material-ui/core";
import { grey, common } from "@material-ui/core/colors";
import { darken } from "polished";

import uoneBlue from "./colors/blue";
import uoneLightBlue from "./colors/lightBlue";
import neonGreen from "./colors/neonGreen";
import paleYellow from "./colors/paleYellow";
import shineViolet from "./colors/shineViolet";
import uoneLightGreen from "./colors/lightGreen";
import frolyRed from "./colors/frolyRed";
import mediumPurple from "./colors/mediumPurple";
import shadows from "./shadows";

const defaultVariant = {
  name: "DEFAULT",
  palette: {
    type: "light" as PaletteType,
    primary: {
      main: uoneBlue[700],
      contrastText: common.white,
    },
    secondary: {
      main: uoneBlue[500],
      contrastText: common.white,
    },
    background: {
      default: "#F7F9FC",
      paper: common.white,
    },
  },
  header: {
    color: common.black,
    background: common.white,
  },
  footer: {
    color: grey[500],
    background: common.white,
  },
  sidebar: {
    background: grey[900],
    color: grey[200],
    menu: {
      background: grey[900],
      color: grey[200],
      fontSize: "1rem",
      iconFontSize: "1.5rem",
      iconMinWidth: "3rem",
      active: {
        background: darken(0.05, grey[800]),
        color: common.white,
      },
    },
    header: {
      color: grey[200],
      background: grey[800],
    },
    footer: {
      color: grey[200],
      background: "#1E2A38",
    },
  },
  common: {
    grey,
    uoneBlue,
    uoneLightBlue,
    uoneNeonGreen: neonGreen,
    uonePaleYellow: paleYellow,
    uoneShineViolet: shineViolet,
    uoneLightGreen,
    uoneFrolyRed: frolyRed,
    uoneMediumPurple: mediumPurple,
  },
  shadows,
};

const variants: Array<ThemeOptions> = [defaultVariant];

export default variants;
