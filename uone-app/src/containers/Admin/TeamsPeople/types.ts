interface UserAttributes {
  clientId: string;
  departmentId: string;
  enabled: number;
  firstName: string;
  lastName: string;
  pointsBalance: number;
  pointsCumulative: number;
  roleId: string;
  username: string;
}

interface UserMask {
  firstName: string;
  lastName: string;
  roleId: string;
  username: string;
}



export interface User {
  created_at: string;
  entityId: string;
  clientId: string;
  type: string;
  attributes: UserAttributes;
  cognitoIdentityId: string;
  departmentUOneId: string;
  departmentId: string;
  roleId: string;
  userUOneId: string;
  mask: UserMask | undefined;
}
export interface PeopleItem {
  agentName: string;
  clientId: string;
  firstName: string;
  lastName: string;
  profileImg: null;
  roleName: string;
  role_id: string;
  teamId: string;
  teamName: string;
  user_id: string;
  username: string;
}
export interface PeopleItems extends Array<PeopleItem> { }

export interface TeamsNameEditIProps {
  user: User;
}

export default TeamsNameEditIProps;
