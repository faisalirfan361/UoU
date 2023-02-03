import Challenges from "pages/Challenges";
import CoinStore from "pages/CoinStore";
import Goals from "pages/Goals";
import Importer from "pages/Importer";
import MyTeam from "pages/MyTeam";
import Reporting from "pages/Reporting";
import Settings from "pages/Settings";
import Leaderboard from "pages/Leaderboard";
import LeaderboardPresentation from "pages/Presentation/Leaderboard";
import Dashboard from "pages/Dashboard";
import MyPerformance from "pages/MyPerformance";
import {
  FaChartLine,
  FaCloudUploadAlt,
  FaCog,
  FaFlag,
  FaMountain,
  FaShoppingCart,
  FaTachometerAlt,
  FaUsers,
  FaUserShield,
  FaChartBar,
} from "react-icons/fa";
import Organization from "pages/Organization";

const protectedRoutes = [
  {
    slug: "dashboard",
    label: "Dashboard",
    path: "/dashboard",
    icon: <FaTachometerAlt />,
    component: Dashboard,
    exact: true,
    only: null,
  },
  {
    slug: "my-performance",
    label: "My Performance",
    path: "/my-performance",
    icon: <FaTachometerAlt />,
    component: MyPerformance,
    exact: true,
    only: null,
  },
  {
    slug: "leaderboard",
    label: "Leaderboard",
    path: "/leaderboard",
    icon: <FaChartBar />,
    component: Leaderboard,
    exact: true,
    only: null,
    exclude: null,
  },
  {
    slug: "organization",
    label: "Organization",
    path: "/organization",
    icon: <FaUsers />,
    component: Organization,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "games",
    label: "Games",
    path: "/games",
    icon: <FaFlag />,
    component: Challenges,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "coin-store",
    label: "Coin Store",
    path: "/store",
    icon: <FaShoppingCart />,
    component: CoinStore,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "goals",
    label: "Goals",
    path: "/goals",
    icon: <FaMountain />,
    component: Goals,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "importer",
    label: "Importer",
    path: "/importer",
    icon: <FaCloudUploadAlt />,
    component: Importer,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "my-team",
    label: "My Team",
    path: "/my-team",
    icon: <FaUserShield />,
    component: MyTeam,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "reporting",
    label: "Reporting",
    path: "/reporting",
    icon: <FaChartLine />,
    component: Reporting,
    guard: true,
    only: null,
    exclude: null,
  },
  {
    slug: "leaderboard-presentation",
    label: "Leaderboard",
    path: "/dashboard",
    icon: <FaChartBar />,
    component: LeaderboardPresentation,
    exact: true,
    only: null,
    exclude: null,
  },

  {
    slug: "settings",
    label: "Settings",
    path: "/settings",
    icon: <FaCog />,
    component: Settings,
    guard: true,
    only: null,
    exclude: null,
  },
];

export default protectedRoutes;
