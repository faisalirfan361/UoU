import React, { FC } from "react";
import { Box, Grid, Typography } from "@material-ui/core";

import useStyles from "./style";
import ActiveDuelPostImgProps from "./types";
import StyledAvatar from "components/StyledAvatar";

const ActiveDuelPostImg: FC<ActiveDuelPostImgProps> = ({
  userOneProfileImgSrc,
  userOneCoverImgSrc,
  userTwoProfileImgSrc,
  userTwoCoverImgSrc,
  user,
  opponent,
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
          <Box className={`${classesVS.block} ${classesVS.blockLeft}`}></Box>
          <Box className={`${classesVS.block} ${classesVS.blockRight}`}>
            <Box className={classesVS.blockRightContainer} />
          </Box>
        </Box>
      </Box>
      <Box className={classesVS.vsContainerB}>
        <Grid container spacing={0}>
          <Grid item className={classesVS.vsItemLeft} xs>
            <Box
              className={classesVS.vsLeft}
              display="flex"
              justifyContent="center"
              alignItems="center"
            >
              <Grid
                container
                direction="column"
                justifyContent="center"
                alignItems="center"
                spacing={1}
              >
                <Grid item xs={12} style={{ padding: "30px 3px" }}>
                  <StyledAvatar
                    size={2}
                    className={classesVS.leftImage}
                    src={userOneProfileImgSrc}
                  />
                </Grid>
                <Grid item xs={"auto"}>
                  <Grid item className={classesVS.vsLeftDetailsBox}>
                    <Box
                      component={"span"}
                      alignItems="center"
                      className={classesVS.vsLeftPosition}
                    >
                      <Typography
                        component={"span"}
                        className={classesVS.vsLeftPositionText}
                      >
                        2
                      </Typography>
                    </Box>
                    <Typography
                      component={"span"}
                      className={classesVS.vsLeftNameText}
                    >
                      {`${opponent.name}`}
                    </Typography>
                    <Typography
                      component={"span"}
                      className={classesVS.vsLeftPointText}
                    >
                      {`${parseFloat(opponent.score?.toFixed(2))} points`}
                    </Typography>
                  </Grid>
                </Grid>
              </Grid>
            </Box>
          </Grid>
          <Grid item className={classesVS.vsItemCenter} xs>
            <Box
              className={classesVS.vsCenter}
              display="flex"
              justifyContent="center"
              alignItems="center"
            >
              <Box
                className={classesVS.vsCenterVSTextWrapper}
                justifyContent="center"
                alignItems="center"
              >
                <Typography className={classesVS.vsCenterVSText}>VS</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid item className={classesVS.vsItemRight} xs>
            <Box
              className={classesVS.vsRight}
              display="flex"
              justifyContent="center"
              alignItems="center"
            >
              <Grid
                container
                direction="column"
                justifyContent="center"
                alignItems="center"
                spacing={1}
              >
                <Grid item xs={12} style={{ padding: "30px 0px" }}>
                  <StyledAvatar
                    size={2}
                    className={classesVS.rightImage}
                    src={userTwoProfileImgSrc}
                  />
                </Grid>
                <Grid item xs={"auto"}>
                  <Grid item className={classesVS.vsRightDetailsBox}>
                    <Box
                      component={"span"}
                      alignItems="center"
                      className={classesVS.vsRightPosition}
                    >
                      <Typography
                        component={"span"}
                        className={classesVS.vsRightPositionText}
                      >
                        1
                      </Typography>
                    </Box>
                    <Typography
                      component={"span"}
                      className={classesVS.vsRightNameText}
                    >
                      {`${user.name}`}
                    </Typography>
                    <Typography
                      component={"span"}
                      className={classesVS.vsRightPointText}
                    >
                      {`${parseFloat(user.score?.toFixed(2))} points`}
                    </Typography>
                  </Grid>
                </Grid>
              </Grid>
            </Box>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default ActiveDuelPostImg;
