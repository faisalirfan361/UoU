import { Theme, ThemeOptions } from "@material-ui/core/styles";

declare module "@material-ui/core" {
  interface CustomMuiColorPalette {
    50: string;
    100: string;
    200: string;
    300: string;
    400: string;
    500: string;
    600: string;
    700: string;
    800: string;
    900: string;
    A100?: string;
    A200?: string;
    A400?: string;
    A700?: string;
  }

  interface ThemeOptions {
    name?: string;
    header?: {
      color?: string;
      background?: string;
    };
    footer?: {
      color?: string;
      background?: string;
    };
    sidebar?: {
      color?: string;
      background?: string;
      header?: {
        color?: string;
        background?: string;
      };
      footer?: {
        color?: string;
        background?: string;
      };
      menu?: {
        color?: string;
        background?: string;
        fontSize?: string;
        iconFontSize?: string;
        iconMinWidth?: string;
        active: {
          color?: string;
          background?: string;
        };
      };
    };
    common?: {
      grey: CustomMuiColorPalette;
      uoneBlue: CustomMuiColorPalette;
      uoneLightBlue: CustomMuiColorPalette;
      uoneNeonGreen: CustomMuiColorPalette;
      uoneShineViolet: CustomMuiColorPalette;
      uonePaleYellow: CustomMuiColorPalette;
      [key: string]: any;
    };
  }

  interface Theme {
    name: string;
    common: {
      grey: CustomMuiColorPalette;
      uoneBlue: CustomMuiColorPalette;
      uoneLightBlue: CustomMuiColorPalette;
      uoneNeonGreen: CustomMuiColorPalette;
      uoneShineViolet: CustomMuiColorPalette;
      uonePaleYellow: CustomMuiColorPalette;
      [key: string]: any;
    };
    header: ColorBgType;
    footer: ColorBgType;
    sidebar: SidebarMenuType;
  }
}
