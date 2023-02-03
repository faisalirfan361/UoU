import { Avatar, Badge, Menu, MenuItem } from "@material-ui/core";

import EditButton from "components/EditButton";
import { useEffect, useState } from "react";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import { useBadgeAvatarStyles, useMainHeaderStyles } from "../styles";
import config from "../../../config";
import UploadImageDialog from "containers/UploadImageDialog";
import { FaUser } from "react-icons/fa";

export default function BadgeAvatar({ position }: any) {
  const { firstName, avatarImages } = useRecoilValue(userAtom);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const badgeStyles = useBadgeAvatarStyles({ position });
  const mainHeaderStyles = useMainHeaderStyles({});
  const imgPath = `${config.targetBucketUrl}${avatarImages?.keys.large}`;

  return (
    <>
      <Badge
        classes={badgeStyles}
        badgeContent={
          <EditButton
            disableRipple={false}
            onClick={() => setUploadDialogOpen(true)}
          />
        }
        overlap="circle"
      >
        <Avatar
          alt={firstName}
          className={mainHeaderStyles.largeAvatar}
          src={imgPath}
        >
          <FaUser />
        </Avatar>
      </Badge>
      <UploadImageDialog
        onClose={() => setUploadDialogOpen(false)}
        open={uploadDialogOpen}
        title="Edit Profile - Avatar"
        type="AVATAR"
      />
    </>
  );
}
