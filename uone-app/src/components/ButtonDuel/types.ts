import React from "react";

interface ButtonDuelProps {
  children: React.ReactNode;
  handleOnClick(): void;
  disabled?: boolean;
}

export default ButtonDuelProps;