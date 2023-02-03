import React, { FC } from "react";
import { Controller } from "react-hook-form";
import Switch, { SwitchProps } from "@material-ui/core/Switch";
import IProps from "./types";
import { styled } from "@material-ui/core";
// import useStyle from "./style";

const IOSSwitch = styled((props: SwitchProps) => (
  <Switch focusVisibleClassName=".Mui-focusVisible" disableRipple {...props} />
))(({ theme }) => ({
  width: 40,
  height: 24,
  padding: 0,
  "& .MuiSwitch-switchBase": {
    padding: 0,
    margin: 2,
    transitionDuration: "300ms",
    "&.Mui-checked": {
      transform: "translateX(16px)",
      color: "#fff",
      "& + .MuiSwitch-track": {
        backgroundColor: theme.palette.type === "dark" ? "#2ECA45" : "#2fb0d9",
        opacity: 1,
        border: 0,
      },
      "&.Mui-disabled + .MuiSwitch-track": {
        opacity: 0.5,
      },
      "& .MuiSwitch-thumb": {
        boxSizing: "border-box",
        width: 20,
        height: 20,
      },
    },
    "&.Mui-focusVisible .MuiSwitch-thumb": {
      color: "#33cf4d",
      border: "6px solid #fff",
    },
    "&.Mui-disabled .MuiSwitch-thumb": {
      color:
        theme.palette.type === "light"
          ? theme.palette.grey[100]
          : theme.palette.grey[600],
    },
    "&.Mui-disabled + .MuiSwitch-track": {
      opacity: theme.palette.type === "light" ? 0.7 : 0.3,
    },
  },
  "& .MuiSwitch-thumb": {
    boxSizing: "border-box",
    width: 20,
    height: 20,
  },
  "& .MuiSwitch-track": {
    borderRadius: 26 / 2,
    width: 50,
    backgroundColor: theme.palette.type === "light" ? "#E9E9EA" : "#39393D",
    opacity: 1,
    transition: theme.transitions.create(["background-color"], {
      duration: 500,
    }),
  },
}));

const RHFIOSSwitchComponent: FC<IProps> = ({
  name,
  value,
  onChange,
  disabled,
}) => {
  // const classes = useStyle();

  return (
    <IOSSwitch
      id={`id-switch-${name}`}
      onChange={(e) => onChange()}
      checked={value}
      defaultChecked={value}
      disabled={disabled}
    />
  );
};

export default RHFIOSSwitchComponent;
