import { atom } from "recoil";

import { challenges } from "./mock";

export const CHALLENGES_ATOM_KEY = "challengeAtom";

export const challengeAtom = atom({
  key: CHALLENGES_ATOM_KEY,
  default: challenges,
});

export default challengeAtom;
