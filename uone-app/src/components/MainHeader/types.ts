import BadgeAvatar from "./components/BadgeAvatar";
import MainHeaderBanner from "./components/MainHeaderBanner";
import PerformanceBar from "./components/PerformanceBar";

export interface MainHeaderCompositionProps {
  BadgeAvatar: typeof BadgeAvatar;
  HeaderBanner: typeof MainHeaderBanner;
  PerformanceBar: typeof PerformanceBar;
}
