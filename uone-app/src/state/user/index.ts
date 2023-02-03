import { atom } from "recoil";

import UserAtomStateType from "./types";

export const USER_ATOM_KEY = "userAtom";

const userDefault: UserAtomStateType = {
  //
  // Cognito Login
  //
  cognitoIdentityId: "",
  email: "",
  emailVerified: false,
  phoneNumber: "",
  phoneNumberVerified: false,
  //
  // User - ProfileFindOne
  //
  // Response - ESSENTIALS
  username: "",
  userId: "",
  roleId: "",
  roleName: "",
  clientId: "",
  clientName: "",
  departmentId: "",
  departmentName: "",
  // Response - REST
  profileImg: "",
  fullName: "",
  firstName: "",
  lastName: "",
  pointsBalance: 0,
  pointsCumulative: 0,
  kpis: [],
  modules: [],
  avatarImages: undefined,
  bannerImages: undefined,
  uoneData: {},
  imageUploaded: new Date(),
  jwtToken: "",
};

/**
 * User State - Create or Rehydrate the Atom.
 */
const userAtom = atom({
  key: USER_ATOM_KEY,
  default: userDefault,
  // @ts-ignore
  persistence_UNSTABLE: {
    type: USER_ATOM_KEY,
  },
});

export default userAtom;
