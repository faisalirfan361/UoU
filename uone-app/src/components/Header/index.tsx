import React, { FC } from "react";
import { useHistory } from "react-router-dom";

import IProps from "./types";
import useStyles from "./style";

import logo from "assets/tempLogo.png";
import { Menu as MenuIcon } from "@material-ui/icons";

import {
  Box,
  IconButton,
  Menu,
  MenuItem,
  Typography,
  Divider,
} from "@material-ui/core";

const options = [
  "Overall Score",
  "First Name",
  "Last Name",
  "BT",
  "BBT",
  "ACW",
  "RC",
  "RT",
  "AHT",
  "FCR",
];

const Header: FC<IProps> = ({ departmentName }) => {
  const classes = useStyles();
  const history = useHistory();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = (sort: string) => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    setAnchorEl(null);
    history.push("/logout");
  };

  return (
    <Box
      className={classes.root}
      display="flex"
      justifyContent="flex-end"
      alignItems="center"
    >
      <img className={classes.logo} alt="Heyday Now logo" src={logo} />

      <Box display="flex" alignItems="center" mr={3}>
        <Box component="span" mr={2}>
          <Typography variant="h5" className={classes.customerName}>
            {departmentName}
          </Typography>
        </Box>
        <IconButton onClick={handleClick}>
          <MenuIcon />
        </IconButton>
      </Box>

      <Menu
        id="long-menu"
        keepMounted
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        classes={{ list: classes.list }}
      >
        <Box mb={1}>
          <Typography align="center" className={classes.menuTitle}>
            Agent Screen
          </Typography>
        </Box>

        <Divider variant="middle" color="secondary" />
        <Box component="span" m={2}>
          <Typography className={classes.menuCategory} variant="caption">
            Sort team by:
          </Typography>
        </Box>

        {options.map((option) => (
          <MenuItem key={option} onClick={() => handleClose(option)}>
            {option}
          </MenuItem>
        ))}

        <Divider variant="middle" color="secondary" />
        <Box component="span" m={2}>
          <Typography className={classes.menuCategory} variant="caption">
            Account:
          </Typography>
        </Box>
        <MenuItem onClick={handleLogout}>Log out</MenuItem>
      </Menu>
    </Box>
  );
};

export default Header;
