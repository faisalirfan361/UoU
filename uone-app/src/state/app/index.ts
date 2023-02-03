import { atom } from "recoil";

import IAppAtomState from "./types";

export const APP_ATOM_KEY = "appAtom";

const appDefault: IAppAtomState = {
  isAuthenticated: false,
  isInProgress: false,
  sidebarOpen: false,
  showNotificationsDrawer: false,
};

/**
 * App State - Create or Rehydrate the Atom.
 */
const appAtom = atom({
  key: APP_ATOM_KEY,
  default: appDefault,
  // @ts-ignore
  persistence_UNSTABLE: {
    type: APP_ATOM_KEY,
  },
});

export default appAtom;
