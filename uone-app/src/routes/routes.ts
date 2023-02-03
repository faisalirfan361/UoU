import React from "react";

export type RouteType = {
  slug: string;
  path: string;
  icon?: JSX.Element | any;
  component: React.FunctionComponent<any> | React.ComponentClass<any>;
  label: string;
  guard?: boolean;
  hiddenMenu?: boolean;
};
