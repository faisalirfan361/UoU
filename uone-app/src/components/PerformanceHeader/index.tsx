import React, { useState } from "react";
import Grid from "@material-ui/core/Grid";

import IProps from "./types";
import useStyle from "./style";
import Avatar from "components/Avatar";
import FlagIcon from "@material-ui/icons/Flag";
import StarIcon from "@material-ui/icons/Star";
import CoinsIcon from "@material-ui/icons/MonetizationOn";
import Typography from "@material-ui/core/Typography";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import config from "config";
import UploadImageDialog from "containers/UploadImageDialog";
import StyledAvatar from "components/StyledAvatar";

const PerformanceHeaderComponent: React.FC<IProps> = ({
  imageUrl,
  challengesWon,
  points,
  coins,
  minimal = false,
}) => {
  const styles = useStyle();
  const { bannerImages, departmentName } = useRecoilValue(userAtom);

  let [modalIsOpen, setModalIsOpen] = useState(false);
  let [uploadMsg, setUploadMsg] = useState("");
  let basPathBanner = config.targetBucketUrl + `${bannerImages?.keys.source}`;

  const setModalIsOpenToTrue = () => {
    setModalIsOpen(true);
  };

  const setModalIsOpenToFalse = () => {
    setModalIsOpen(false);
  };

  const setModalIsUploadedToTrue = (setTo: any) => {
    if (!setTo) return "BANNER";
    setUploadMsg(setTo);
  };

  const avatarExtraStyles = {
    width: minimal ? 60 : 120,
    height: minimal ? 60 : 120,
  };
  return (
    <>
      <UploadImageDialog type="BANNER" />
      {minimal === false && (
        <Grid
          container
          direction="row"
          className={styles.backgroundImage}
          style={{
            background: "url('" + basPathBanner + "')",
          }}
          onClick={setModalIsOpenToTrue}
        ></Grid>
      )}
      <Grid container direction="row" className={styles.container}>
        <Grid item xs={4} className={`${styles.flexStart}`}>
          <StyledAvatar src={imageUrl} extraStyles={avatarExtraStyles} />
          <div>
            <Typography
              className={`${styles.text} ${styles.boldText}`}
              variant="subtitle2"
            >
              Team
            </Typography>
            <Typography className={styles.text} variant="subtitle2">
              {departmentName}
            </Typography>
          </div>
        </Grid>
        <Grid item xs={2} className={`${styles.flexStart}`}></Grid>
        <Grid item xs={2} className={`${styles.flexStart}`}>
          <FlagIcon />
          <div>
            <Typography variant="subtitle2" className={`${styles.count}`}>
              {challengesWon}
            </Typography>
            <Typography className={styles.countLabel} variant="body2">
              Wins
            </Typography>
          </div>
        </Grid>
        <Grid item xs={2} className={`${styles.flexStart}`}>
          <StarIcon />
          <div>
            <Typography className={`${styles.count}`} variant="subtitle2">
              {points}
            </Typography>
            <Typography className={`${styles.countLabel}`} variant="subtitle2">
              Points
            </Typography>
          </div>
        </Grid>
        <Grid item xs={2} className={`${styles.flexStart}`}>
          <CoinsIcon />
          <div>
            <Typography className={`${styles.count}`} variant="subtitle2">
              {coins}
            </Typography>
            <Typography className={`${styles.countLabel}`} variant="subtitle2">
              Coins
            </Typography>
          </div>
        </Grid>
      </Grid>
    </>
  );
};

export default PerformanceHeaderComponent;
