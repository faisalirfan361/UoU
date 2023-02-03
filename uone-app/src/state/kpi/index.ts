import { atom } from "recoil";

import { kpis } from "./mock";

export const KPI_ATOM_KEY = "kpiAtom";

export const kpiAtom = atom({
  key: KPI_ATOM_KEY,
  default: kpis,
});

export default kpiAtom;
