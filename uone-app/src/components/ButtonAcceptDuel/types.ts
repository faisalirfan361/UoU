import React from "react";

interface AcceptDuelProps {
  children: React.ReactNode;
  handleOnClick(): void;
  disabled?: boolean;
}

export default AcceptDuelProps;
