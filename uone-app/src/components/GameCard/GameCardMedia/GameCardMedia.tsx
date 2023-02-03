import { Box, CardMedia } from "@material-ui/core";
import { FC } from "react";

import useMediaStyles from "./styles";
import challengesCardBanner from "../../../assets/img/challenges/banner.png";

export interface GameCardMediaProps
  extends React.HTMLAttributes<HTMLDivElement> {
  image?: string;
  title?: string;
}

const GameCardMedia: FC<GameCardMediaProps> = ({
  image = challengesCardBanner,
  title,
  ...props
}) => {
  const baseClass = useMediaStyles({ image: challengesCardBanner });

  return (
    <Box paddingLeft="1em" paddingRight="1em">
      <Box className={baseClass.wrapper}>
        <Box className={baseClass.media} />
      </Box>
    </Box>
  );
};

export default GameCardMedia;
