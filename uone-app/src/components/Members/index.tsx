import React, { FC, memo } from "react";
import Box from "@material-ui/core/Box";

import { BoxMember } from "components";

import IProps from "./types";

import useStyles from "./style";

const Members: FC<IProps> = ({ members }) => {
  const classes = useStyles();

  return (
    <Box
      className={classes.root}
      display="flex"
      flexWrap="wrap"
      alignContent="flex-start"
    >
      {members.map((member: any, index: number) => (
        <Box className={classes.member} key={index}>
          <BoxMember user={member} type={"team"} />
        </Box>
      ))}
    </Box>
  );
};

export default memo(Members);
