import React, { FC } from "react";

import IProps from "./types";

import { Box, Divider, Typography } from "@material-ui/core";

import useStyles from "./style";

const Messages: FC<IProps> = ({ messages }) => {
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

export default Messages;
