import Error from "pages/Error";
import Home from "pages/Home";
import Logout from "pages/Logout";

const publicRoutes = [
  {
    slug: "logout",
    label: "Logout",
    path: "/logout",
    component: Logout,
    exact: true,
    only: null,
    exclude: null,
  },
  {
    slug: "error",
    label: "Error",
    path: "/error",
    component: Error,
    exact: true,
    only: null,
    exclude: null,
  },
  {
    slug: "home",
    label: "Home",
    path: "/",
    component: Home,
    only: null,
    exclude: null,
  },
];

export default publicRoutes;
