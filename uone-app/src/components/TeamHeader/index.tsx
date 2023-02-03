import React from "react";
import Grid from "@material-ui/core/Grid";
import useStyle from "./style";
import Avatar from "components/Avatar";
import IProps from "./types";

export const TeamHeader: React.FC<IProps> = ({
  backgroundUrl,
  avatarUrl,
  avatarInitials,
}) => {
  const styles = useStyle();

  return (
    <>
      <Grid container direction="row" className={styles.container}>
        <Grid
          item
          xs={12}
          className={styles.feedItemCover}
          style={{
            background: backgroundUrl ? `url('${backgroundUrl}')` : undefined,
          }}
        >
          <Avatar
            dimension={120}
            className={styles.feedItemAvatarCenterBottom}
            src={avatarUrl}
            initials={avatarInitials}
          />
        </Grid>
      </Grid>
    </>
  );
};

TeamHeader.defaultProps = {
  backgroundUrl: "https://picsum.photos/1000",
  avatarUrl: "https://picsum.photos/1500",
  avatarInitials: "XD",
};

export default TeamHeader;
