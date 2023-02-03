import React from "react";

import {
  Grid,
  Hidden,
  List,
  ListItemText,
  ListItem,
  Box,
} from "@material-ui/core";

import useStyles from "./styles";

const DashboardFooter = () => {
  const classes = useStyles();
  return (
    <Box className={classes.root}>
      <Grid container spacing={0}>
        <Hidden smDown>
          <Grid container item xs={12} md={8}>
            <List>
              <ListItem
                button
                component="a"
                href="#"
                className={classes.listItem}
              >
                <ListItemText
                  primary="Support"
                  className={classes.listItemText}
                />
              </ListItem>
              <ListItem
                button
                component="a"
                href="#"
                className={classes.listItem}
              >
                <ListItemText
                  primary="Help Center"
                  className={classes.listItemText}
                />
              </ListItem>
              <ListItem
                button
                component="a"
                href="#"
                className={classes.listItem}
              >
                <ListItemText
                  primary="Privacy"
                  className={classes.listItemText}
                />
              </ListItem>
              <ListItem
                button
                component="a"
                href="#"
                className={classes.listItem}
              >
                <ListItemText
                  primary="Terms of Service"
                  className={classes.listItemText}
                />
              </ListItem>
            </List>
          </Grid>
        </Hidden>
      </Grid>
    </Box>
  );
};

export default DashboardFooter;
