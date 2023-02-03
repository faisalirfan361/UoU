import React from "react";

interface ButtonDeleteItemProps {
  children: React.ReactNode;
  handleOnClick: () => void;
  disabled?: boolean;
}

export default ButtonDeleteItemProps;
