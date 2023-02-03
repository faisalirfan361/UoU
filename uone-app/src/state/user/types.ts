interface UserAtomStateType {
  //
  // Cognito Login
  //
  cognitoIdentityId: string;
  email: string;
  emailVerified: boolean;
  phoneNumber: string;
  phoneNumberVerified: boolean;
  //
  // User - ProfileFindOne
  //
  // Response - ESSENTIALS
  username: string;
  userId: string;
  roleId: string;
  roleName: string;
  clientId: string;
  clientName: string;
  departmentId: string;
  departmentName: string;
  // Response - REST
  profileImg: string;
  fullName: string;
  firstName: string;
  lastName: string;
  pointsBalance: number;
  pointsCumulative: number;
  kpis: [];
  modules: [];
  avatarImages: any | undefined;
  bannerImages: any | undefined;
  uoneData: any | undefined;
  imageUploaded: Date;
  jwtToken: string;
}

export default UserAtomStateType;
