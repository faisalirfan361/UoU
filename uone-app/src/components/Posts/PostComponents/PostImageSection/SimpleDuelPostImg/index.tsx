import React, { FC } from "react";
import { Box, Grid, Typography } from "@material-ui/core";

import useStyles from "./style";
import IProps from "./types";
import StyledAvatar from "components/StyledAvatar";

const SimpleDuelPostImg: FC<IProps> = ({
  userOneProfileImgSrc,
  userOneCoverImgSrc,
  userTwoProfileImgSrc,
  userTwoCoverImgSrc,
}) => {
  const DuelCustomStyleProps = {
    agent1: userOneCoverImgSrc,
    agent2: userTwoCoverImgSrc,
  };

  const classesVS = useStyles(DuelCustomStyleProps)();
  return (
    <Box className={classesVS.vsContainer}>
      <Box className={classesVS.vsContainerA}>
        <Box className={classesVS.blocks}>
          <Box className={`${classesVS.block} ${classesVS.blockLeft}`} />
          <Box className={`${classesVS.block} ${classesVS.blockRight}`} />
        </Box>
      </Box>
      <Box className={classesVS.vsContainerB}>
        <Grid container spacing={0}>
          <Grid item className={classesVS.vsItemLeft} xs>
            <Box
              className={classesVS.vsLeft}
              display="flex"
              justifyContent="flex-end"
              alignItems="center"
            >
              <StyledAvatar
                size={2}
                className={classesVS.leftImage}
                src={userOneProfileImgSrc}
              />
            </Box>
          </Grid>
          <Grid item className={classesVS.vsItemCenter} xs>
            <Box
              className={classesVS.vsCenter}
              display="flex"
              justifyContent="center"
              alignItems="center"
            >
              <Box>
                <Typography className={classesVS.vsCenterVSText}>VS</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid item className={classesVS.vsItemRight} xs>
            <Box
              className={classesVS.vsRight}
              display="flex"
              justifyContent="flex-start"
              alignItems="center"
            >
              <StyledAvatar
                size={2}
                className={classesVS.rightImage}
                src={userTwoProfileImgSrc}
              />
            </Box>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default SimpleDuelPostImg;
