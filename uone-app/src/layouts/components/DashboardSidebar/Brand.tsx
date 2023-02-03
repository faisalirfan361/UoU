import { NavLink } from "react-router-dom";

import useSidebarClasses from "./styles";
import logo from "../../../assets/img/logo.png";
import { Box } from "@material-ui/core";

const Brand = () => {
  const classes = useSidebarClasses();

  return (
    <>
      <Box className={classes.sidebarHeader}>
        <NavLink to="/" className={classes.brand}>
          <img src={logo} alt="HeyDay Now" />
        </NavLink>
      </Box>
    </>
  );
};

export default Brand;
