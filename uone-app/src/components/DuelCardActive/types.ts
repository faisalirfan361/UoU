import React from "react";
import { DuelType } from "components/GameCard/type";

interface DuelActiveProps {
  duel: DuelType
  refresh?():  void
}
export default DuelActiveProps;