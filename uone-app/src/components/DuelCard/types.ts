import React from "react";
import { DuelType } from "components/GameCard/type";

interface DuelProps {
  duel: DuelType
  refresh?(): void
  filter: string
}

export default DuelProps;