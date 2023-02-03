# Generate custom colors

GO to http://mcg.mbitson.com/
Add a hex color and hit choose.
A new palette is generated use the following template to create a new file and register it on common variants.

```typescript
const uoneLightBlue050 = "#e6f6fa";
const uoneLightBlue0100 = "#c1e7f4";
const uoneLightBlue0200 = "#97d8ec";
const uoneLightBlue0300 = "#6dc8e4";
const uoneLightBlue0400 = "#4ebcdf";
const uoneLightBlue0500 = "#2fb0d9";
const uoneLightBlue0600 = "#2aa9d5";
const uoneLightBlue0700 = "#23a0cf";
const uoneLightBlue0800 = "#1d97ca";
const uoneLightBlue0900 = "#1287c0";
const uoneLightBlue0A100 = "#f0faff";
const uoneLightBlue0A200 = "#bde7ff";
const uoneLightBlue0A400 = "#8ad5ff";
const uoneLightBlue0A700 = "#70ccff";

export default {
  50: uoneLightBlue050,
  100: uoneLightBlue0100,
  200: uoneLightBlue0200,
  300: uoneLightBlue0300,
  500: uoneLightBlue0400,
  400: uoneLightBlue0500,
  600: uoneLightBlue0600,
  700: uoneLightBlue0700,
  800: uoneLightBlue0800,
  900: uoneLightBlue0900,
  A100: uoneLightBlue0A100,
  A200: uoneLightBlue0A200,
  A400: uoneLightBlue0A400,
  A700: uoneLightBlue0A700,
};
```

Go to `variants.ts` and register the new palette under `common` make sure to a key to the types
