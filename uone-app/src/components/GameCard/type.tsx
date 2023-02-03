export interface ClientType {
  client_id: number | string;
  cname: string;
  cstatus: string;
}

export interface UserToDepartmentType {
  client_id: number | string;
  department_id: number | string;
  dname: string;
  dstatus: string;
  store_id: number | string;
}

export interface ChallengeProfileUserType {
  user_id: number | string;
  role_id: number | string;
  client_id: number | string;
  username: string;
  lastName: string;
  firstName: string;
  password?: string;
  thirdPartyID?: string | number;
  integrationID?: string | number;
  cognitoIdentityId: string | number;
  profileImg?: string;
  pointsBalance: number;
  pointsCumulative: number;
  ustatus: string;
  signon_ip: string;
  signon_date: string;
  last_ip?: string;
  last_login?: string;
  repTileBgImg?: string;
  badgesJSON?: string;
  teamJSON?: string;
  createdAt: string;
  updatedAt: string;
  enabled: boolean;
  Client: ClientType;
  USER_TO_DEPARTMENT: UserToDepartmentType[];
}

export interface ChallengeUserProfileType {
  entityId: string;
  departmentId: string;
  lastName: string;
  firstName: string;
  profileImg?: string;
  score?: number;
}

export interface KpiSetting {
  goal: number;
  optained: number;
}

export interface ChallengeTeamType {
  dname: string;
  department_id: string | number;
  client_id: string | number;
  DEPARTMENT_TO_MANAGER: DepartmentToManagerType[];
  USER_TO_DEPARTMENT: UserToDepartmentType[];
}

export interface DepartmentToManagerType {
  user_id: string | number;
  role_id: string | number;
  client_id: string | number;
  username: string;
  lastName: string;
  firstName: string;
  password: string;
  thirdPartyID?: string | number;
  integrationID?: string | number;
  cognitoIdentityId: string | number;
  profileImg?: string;
  pointsBalance: number;
  pointsCumulative: number;
  ustatus?: string;
  signon_ip?: string;
  signon_date?: string;
  last_ip?: string;
  last_login?: string;
  repTileBgImg?: string;
  badgesJSON?: string;
  teamJSON?: string;
  createdAt?: string | number;
  updatedAt?: string;
  enabled: boolean;
  USERS_TO_KPI_SETTINGS: UserToKpiSettingsType[];
}

export interface UserToKpiSettingsType {
  name: string;
  kpi_id: string | number;
  code: string;
  KPISettings: KpiSetting;
}

export interface ChallengeProfileType {
  users: ChallengeUserProfileType[];
  teams: ChallengeTeamType[];
}

export interface ChallengeType {
  challenge_id: string;
  kpi_id: string;
  kpi_name?: string;
  client_id: string;
  title: string;
  description: string;
  winnerPoints: string | number;
  start_date: string;
  end_date: string;
  isComplete: string;
  winnerProfile: any[];
  challengesUser: number[];
  teams: string[];
  schedule: string;
  cstatus: boolean;
  profiles: ChallengeUserProfileType[];
  gameId: string;
  created_at: number | string;
  indexed_at: string;
}

export interface DuelType {
  created_at: number | string;
  clientId: string;
  end_date: string;
  isDuel: boolean;
  isComplete: string;
  winnerProfile: any[];
  winnerPoints: string;
  profiles: ChallengeUserProfileType[];
  kpi_id: string;
  kpi_name?: string;
  start_date: string;
  isAccepted: boolean;
  isDeclined: boolean;
  description: string;
  gameId: string;
  title: string;
  user_id: string;
}

export interface GameRankUser {
  rank: number | string;
  fullName: string;
  avatar?: string;
  score: number;
}
