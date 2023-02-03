import { CardMedia } from "@material-ui/core";
import EditButton from "components/EditButton";
import { useEffect, useState } from "react";

import { useMainHeaderStyles } from "../styles";
import { HeroImageProps } from "./types";
import config from "../../../config";
import UploadImageDialog from "containers/UploadImageDialog";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";

export default function MainHeaderBanner({ children }: HeroImageProps) {
  const { bannerImages } = useRecoilValue(userAtom);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const classes = useMainHeaderStyles({});
  const imgPath = `${config.targetBucketUrl}${bannerImages?.keys.source}`;

  return (
    <>
      <CardMedia
        className={classes.heroImage}
        style={{
          backgroundImage: `url('${imgPath}')`,
        }}
      >
        <EditButton
          id="cover-banner-edit-button"
          className={classes.bannerEditButton}
          disableRipple={false}
          onClick={() => setUploadDialogOpen(true)}
        />
        {children}
      </CardMedia>

      <UploadImageDialog
        onClose={() => setUploadDialogOpen(false)}
        open={uploadDialogOpen}
        title="Edit Profile - Cover Photo"
        type="BANNER"
      />
    </>
  );
}
