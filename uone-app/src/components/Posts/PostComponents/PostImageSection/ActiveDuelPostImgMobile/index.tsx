import React, { FC } from "react";
import { Box, Grid, Typography } from "@material-ui/core";

import useStyles from "./style";
import ActiveDuelPostImgMobileProps from "./types";
import StyledAvatar from "components/StyledAvatar";

const ActiveDuelPostImgMobile: FC<ActiveDuelPostImgMobileProps> = ({
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
        <Grid container className={classesVS.blocks}>
          <Box
            width={1}
            className={`${classesVS.block} ${classesVS.blockLeft}`}
          ></Box>
          <Box
            width={1}
            className={`${classesVS.blockRightWrapper} ${classesVS.blockRight}`}
          >
            <Box className={classesVS.blockRightContainer} />
          </Box>
        </Grid>
      </Box>
      <Box className={classesVS.vsContainerB}>
        <Grid container spacing={0}>
          <Grid
            container
            direction="row"
            alignItems="center"
            justifyContent="flex-start"
            spacing={0}
            className={classesVS.vsItemLeft}
          >
            <Grid
              container
              direction="row"
              alignItems="center"
              spacing={0}
              className={classesVS.vsLeft}
            >
              <Grid item xs={"auto"} style={{ padding: "30px 0px" }}>
                <StyledAvatar
                  size={1.5}
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
                    {`${opponent.firstName} ${opponent.lastName}`.slice(0, 8)}.
                  </Typography>
                  <Typography
                    component={"span"}
                    className={classesVS.vsLeftPointText}
                  >
                    {`${opponent.score} points`}
                  </Typography>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
          <Grid
            container
            direction="row"
            alignItems="center"
            justifyContent="flex-end"
            spacing={5}
            className={classesVS.vsItemRight}
          >
            <Grid
              container
              direction="row"
              alignItems="center"
              spacing={5}
              className={classesVS.vsRight}
            >
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
                    {`${user.firstName} ${user.lastName}`.slice(0, 8)}.
                  </Typography>
                  <Typography
                    component={"span"}
                    className={classesVS.vsRightPointText}
                  >
                    {`${user.score} points`}
                  </Typography>
                </Grid>
              </Grid>
              <Grid item xs={"auto"} style={{ padding: "30px 0px" }}>
                <StyledAvatar
                  size={1.5}
                  className={classesVS.rightImage}
                  src={userTwoProfileImgSrc}
                />
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default ActiveDuelPostImgMobile;
