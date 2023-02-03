import React from "react";
import { Link } from "react-router-dom";

import { Button, Typography } from "@material-ui/core";

import useStyles from "./styles";

const Page404 = () => {
  const classes = useStyles();
  return (
    <div className={classes.root}>
      <div>
        <Typography component="h1" variant="h1" align="center" gutterBottom>
          404
        </Typography>
        <Typography component="h2" variant="h5" align="center" gutterBottom>
          Page not found.
        </Typography>
        <Typography component="h2" variant="body1" align="center" gutterBottom>
          The page you are looking for might have been removed.
        </Typography>

        <Button
          size="large"
          component={Link}
          to="/"
          variant="contained"
          color="secondary"
        >
          Return home
        </Button>
      </div>
    </div>
  );
};

export default Page404;
