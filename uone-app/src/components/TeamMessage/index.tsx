import React, { FC } from "react";
import { Box, Divider, Typography } from "@material-ui/core";

import IProps from "./types";
import useStyles from "./style";

const TeamMessage: FC<IProps> = ({ messages }) => {
  const classes = useStyles();
  return (
    <Box
      className={classes.root}
      display="flex"
      flexWrap="wrap"
      alignContent="flex-start"
      textAlign="left"
    >
      {messages.map((message: any, index: number) => (
        <Box width={1} key={index}>
          <Typography className={classes.text} key={index}>
            {message.message}
          </Typography>
          <Divider variant="middle" />
        </Box>
      ))}
    </Box>
  );
};

export default TeamMessage;
