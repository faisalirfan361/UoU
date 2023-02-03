import { Card } from "@material-ui/core";
import { FC } from "react";
import BadgeAvatar from "./components/BadgeAvatar";
import HeaderBanner from "./components/MainHeaderBanner";
import PerformanceBar from "./components/PerformanceBar";
import { useMainHeaderCardStyles } from "./styles";
import { MainHeaderCompositionProps } from "./types";

const MainHeader: FC<{ overflow?: string }> & MainHeaderCompositionProps = ({
  children,
  overflow = "initial",
}) => {
  const cardStyles = useMainHeaderCardStyles({ overflow });

  return <Card classes={cardStyles}>{children}</Card>;
};

MainHeader.HeaderBanner = HeaderBanner;
MainHeader.BadgeAvatar = BadgeAvatar;
MainHeader.PerformanceBar = PerformanceBar;

export default MainHeader;
