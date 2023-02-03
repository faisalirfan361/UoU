import { OptionsObject } from "notistack";
import {
  FaUsers,
  FaTachometerAlt,
  FaMountain,
  FaFontAwesomeFlag,
  FaChartLine,
  FaCog,
  FaShoppingCart,
} from "react-icons/fa";

export const zIndex = {
  select: 1010,
};

export const performanceState = {
  behind: "Behind",
  onTrack: "On Track",
  complete: "Completed",
};

export const GOALS_SETTINGS_TYPE = [
  {
    label: "Number",
    value: "number",
  },
  {
    label: "Percentage",
    value: "percentage",
  },
];

export enum APP_ROLES {
  ADMIN = "admin",
  MANAGER = "manager",
  TEAM_LEAD = "team lead",
  AGENT = "agent",
}

export enum MenuVariant {
  ADMIN,
  MANAGER,
  TEAM_LEAD,
  AGENT,
}

export const adminMenuItems = [
  {
    title: "Dashboard",
    iconClass: FaTachometerAlt,
    path: "/",
    key: "adminDashboard",
  },
  {
    title: "Teams",
    iconClass: FaUsers,
    path: "/teams",
    key: "adminDashboardTeams",
  },
  {
    title: "Goals",
    iconClass: FaMountain,
    path: "/goals",
    key: "adminDashboardGoals",
  },
  {
    title: "Challenges",
    iconClass: FaFontAwesomeFlag,
    path: "/challenges",
    key: "adminDashboardChallenges",
  },
  {
    title: "Coin Store",
    iconClass: FaShoppingCart,
    path: "/store",
    key: "adminDashboardStore",
  },
  {
    title: "Reporting",
    iconClass: FaChartLine,
    path: "/reporting",
    key: "adminDashboardReporting",
  },
  {
    title: "Settings",
    iconClass: FaCog,
    path: "/settings",
    key: "adminDashboardSettings",
  },
];

export const agentMenuItems = [
  {
    title: "My Performance",
    iconClass: FaTachometerAlt,
    path: "/",
    key: "agentPerformance",
  },
  {
    title: "My Team",
    iconClass: FaUsers,
    path: "/my-team",
    key: "agentDashboardMyTeam",
  },
  {
    title: "Games",
    iconClass: FaFontAwesomeFlag,
    path: "/challenges",
    key: "agentDashboardChallenges",
  },
  {
    title: "Coin Store",
    iconClass: FaShoppingCart,
    path: "/store",
    key: "agentDashboardStore",
  },
];

export const teamMenuItems = [
  {
    title: "Dashboard",
    iconClass: FaTachometerAlt,
    path: "/",
    key: "teamDashboard",
  },
  {
    title: "My Team",
    iconClass: FaUsers,
    path: "/my-team",
    key: "teamDashboardTeams",
  },
  {
    title: "Challenges",
    iconClass: FaFontAwesomeFlag,
    path: "/challenges",
    key: "teamDashboardChallenges",
  },
  {
    title: "Coin Store",
    iconClass: FaShoppingCart,
    path: "/store",
    key: "teamDashboardStore",
  },
];

export const managerMenuItems = [
  {
    title: "Dashboard",
    iconClass: FaTachometerAlt,
    path: "/",
    key: "managerDashboard",
  },
  {
    title: "Teams",
    iconClass: FaUsers,
    path: "/teams",
    key: "managerDashboardTeams",
  },
  {
    title: "Games",
    iconClass: FaFontAwesomeFlag,
    path: "/games",
    key: "managerDashboardTeams",
  },
  {
    title: "Settings",
    iconClass: FaCog,
    path: "/settings",
    key: "managerDashboardSettings",
  },
  {
    title: "Goals",
    iconClass: FaMountain,
    path: "/goals",
    key: "managerDashboardGoals",
  },
];

export const LANGUAGE_OPTIONS = [
  {
    value: "EN_US",
    label: "English (US)",
  },
  {
    value: "ES",
    label: "Spanish",
  },
  {
    value: "FR",
    label: "French",
  },
];

export const DEFAULT_ROLES = [
  {
    entityId: "greenix-role-82e54944-6c93-4627-bcfd-e3cb4719e537",
    roleName: "Agent",
  },
  {
    entityId: "greenix-role-bc4e9370-0821-4a75-acc9-1a576a9c6cb3",
    roleName: "Team lead",
  },
  {
    entityId: "greenix-role-d7a3e609-b2d1-4972-bca2-ab403b7c673b",
    roleName: "Manager",
  },
  // {
  //   value: 0,
  //   label: "Admin",
  // },
];

export const RECURRENCE_OPTIONS = [
  {
    value: "DAILY",
    label: "Per Day",
  },
  {
    value: "WEEKLY",
    label: "Per Week",
  },
  {
    value: "MONTHLY",
    label: "Per Month",
  },
  {
    value: "YEARLY",
    label: "Per Year",
  },
];

export const POST_TYPES = {
  SIMPLE_CHALLENGE: "SIMPLE_CHALLENGE",
  GOAL_MET: "GOAL_MET",
  DUEL_CREATED: "DUEL_CREATED",
  CHALLENGE_CREATED: "CHALLENGE_CREATED"
};

export const TIMEZONE_OPTIONS = [
  // {
  //   offset: "-11:00",
  //   label: "(GMT-11:00) Pago Pago",
  //   tzCode: "Pacific/Pago_Pago",
  // },
  // {
  //   offset: "-10:00",
  //   label: "(GMT-10:00) Hawaii Time",
  //   tzCode: "Pacific/Honolulu",
  // },
  // { offset: "-10:00", label: "(GMT-10:00) Tahiti", tzCode: "Pacific/Tahiti" },
  // {
  //   offset: "-09:00",
  //   label: "(GMT-09:00) Alaska Time",
  //   tzCode: "America/Anchorage",
  // },
  // {
  //   offset: "-08:00",
  //   label: "(GMT-08:00) Pacific Time",
  //   tzCode: "America/Los_Angeles",
  // },
  {
    offset: "-07:00",
    label: "(GMT-07:00) Mountain Time",
    tzCode: "America/Denver",
  },
  // {
  //   offset: "-06:00",
  //   label: "(GMT-06:00) Central Time",
  //   tzCode: "America/Chicago",
  // },
  // {
  //   offset: "-05:00",
  //   label: "(GMT-05:00) Eastern Time",
  //   tzCode: "America/New_York",
  // },
  // {
  //   offset: "-04:00",
  //   label: "(GMT-04:00) Atlantic Time - Halifax",
  //   tzCode: "America/Halifax",
  // },
  // {
  //   offset: "-03:00",
  //   label: "(GMT-03:00) Buenos Aires",
  //   tzCode: "America/Argentina/Buenos_Aires",
  // },
  // {
  //   offset: "-02:00",
  //   label: "(GMT-02:00) Sao Paulo",
  //   tzCode: "America/Sao_Paulo",
  // },
  // { offset: "-01:00", label: "(GMT-01:00) Azores", tzCode: "Atlantic/Azores" },
  // { offset: "+00:00", label: "(GMT+00:00) London", tzCode: "Europe/London" },
  // { offset: "+01:00", label: "(GMT+01:00) Berlin", tzCode: "Europe/Berlin" },
  // {
  //   offset: "+02:00",
  //   label: "(GMT+02:00) Helsinki",
  //   tzCode: "Europe/Helsinki",
  // },
  // {
  //   offset: "+03:00",
  //   label: "(GMT+03:00) Istanbul",
  //   tzCode: "Europe/Istanbul",
  // },
  // { offset: "+04:00", label: "(GMT+04:00) Dubai", tzCode: "Asia/Dubai" },
  // { offset: "+04:30", label: "(GMT+04:30) Kabul", tzCode: "Asia/Kabul" },
  // {
  //   offset: "+05:00",
  //   label: "(GMT+05:00) Maldives",
  //   tzCode: "Indian/Maldives",
  // },
  // {
  //   offset: "+05:30",
  //   label: "(GMT+05:30) India Standard Time",
  //   tzCode: "Asia/Calcutta",
  // },
  // {
  //   offset: "+05:45",
  //   label: "(GMT+05:45) Kathmandu",
  //   tzCode: "Asia/Kathmandu",
  // },
  // { offset: "+06:00", label: "(GMT+06:00) Dhaka", tzCode: "Asia/Dhaka" },
  // { offset: "+06:30", label: "(GMT+06:30) Cocos", tzCode: "Indian/Cocos" },
  // { offset: "+07:00", label: "(GMT+07:00) Bangkok", tzCode: "Asia/Bangkok" },
  // {
  //   offset: "+08:00",
  //   label: "(GMT+08:00) Hong Kong",
  //   tzCode: "Asia/Hong_Kong",
  // },
  // {
  //   offset: "+08:30",
  //   label: "(GMT+08:30) Pyongyang",
  //   tzCode: "Asia/Pyongyang",
  // },
  // { offset: "+09:00", label: "(GMT+09:00) Tokyo", tzCode: "Asia/Tokyo" },
  // {
  //   offset: "+09:30",
  //   label: "(GMT+09:30) Central Time - Darwin",
  //   tzCode: "Australia/Darwin",
  // },
  // {
  //   offset: "+10:00",
  //   label: "(GMT+10:00) Eastern Time - Brisbane",
  //   tzCode: "Australia/Brisbane",
  // },
  // {
  //   offset: "+10:30",
  //   label: "(GMT+10:30) Central Time - Adelaide",
  //   tzCode: "Australia/Adelaide",
  // },
  // {
  //   offset: "+11:00",
  //   label: "(GMT+11:00) Eastern Time - Melbourne, Sydney",
  //   tzCode: "Australia/Sydney",
  // },
  // { offset: "+12:00", label: "(GMT+12:00) Nauru", tzCode: "Pacific/Nauru" },
  // {
  //   offset: "+13:00",
  //   label: "(GMT+13:00) Auckland",
  //   tzCode: "Pacific/Auckland",
  // },
  // {
  //   offset: "+14:00",
  //   label: "(GMT+14:00) Kiritimati",
  //   tzCode: "Pacific/Kiritimati",
  // },
];

export const SUCCESS_TOAST_OPTIONS: OptionsObject = {
  variant: "success",
  autoHideDuration: 3000,
  preventDuplicate: true,
};

export const ERROR_TOAST_OPTIONS: OptionsObject = {
  variant: "error",
  autoHideDuration: 5000,
  preventDuplicate: true,
};

export enum DataPoint {
  BT = "bt",
  BB = "bb",
  ACW = "acw",
  RC = "rc",
  RT = "rt",
  AHT = "aht",
  FCR = "fcr",
}

export const CHART_RECURRENCE = [
  // {
  //   label: "Daily",
  //   value: "DAILY",
  // },
  // {
  //   label: "Weekly",
  //   value: "WEEKLY",
  // },
  // {
  //   label: "Monthly",
  //   value: "MONTHLY",
  // },
  // {
  //   label: "Quarterly",
  //   value: "QUARTERLY",
  // },
  {
    label: "Yearly",
    value: "YEARLY",
  },
];

export const ChallengeStates = {
  ACTIVE: "Active",
  EXPIRED: "Expired",
  FUTURE_START: "Future Start",
};

export enum ChallengeRecurrentOptions {
  ONCE = "ONCE",
  DAILY = "DAILY",
  WEEKLY = "WEEKLY",
  MONTHLY = "MONTHLY",
}

export const NotificationsTypes = {
  CHALLENGE_COMMENT: "CHALLENGE_COMMENT",
  CHALLENGE_LIKE: "CHALLENGE_LIKE",
} as const;

export const GOAL_DURATION = [
  {
    value: 1,
    label: "Hourly",
  },
  {
    value: 2,
    label: "Daily",
  },
  {
    value: 3,
    label: "Weekly",
  },
  {
    value: 4,
    label: "Monthly",
  },
];

export const GOAL_TYPE = [
  {
    value: 1,
    label: "Time",
  },
  {
    value: 2,
    label: "Dollar",
  },
  {
    value: 3,
    label: "Number",
  },
  {
    value: 4,
    label: "Percent",
  },
];
