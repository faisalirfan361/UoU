import React, { FC } from "react";
import SinglePostImgProps from "./types";
import {Box, Grid, Typography} from "@material-ui/core";
import useStyles from "./style";
import UserProfileImage from "../../../../../components/UserProfileImage"



const SimpleDuelPostImg: FC<SinglePostImgProps> = ({ userOneProfileImgSrc, userOneCoverImgSrc,
                                         userTwoProfileImgSrc, userTwoCoverImgSrc}) => {
  const classes = useStyles();

  const agentOneCoverImgStyle = {
    backgroundImage: 'url('+ userOneCoverImgSrc+')'
  };
  const agentTwoCoverImgStyle = {
    backgroundImage: 'url('+ userTwoCoverImgSrc+')'
  };

  return (
    <Box
      className={classes.root}
    >
      <Grid container  spacing={0} className={classes.imagesContainer}>
        <Grid item xs={12}>
          <div style={agentOneCoverImgStyle}
               className={classes.agentDivCoverGeneral}>
            <UserProfileImage imgSrc={userOneProfileImgSrc} className={classes.agentOneProfileImg} />
          </div>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SimpleDuelPostImg;
