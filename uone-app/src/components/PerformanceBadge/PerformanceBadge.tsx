import { Box, Tooltip } from "@material-ui/core";
import clsx from "clsx";
import { useMemo } from "react";
import {
  FaAward,
  FaBowlingBall,
  FaBug,
  FaCalendarCheck,
  FaCandyCane,
  FaGamepad,
  FaLevelUpAlt,
  FaStar,
  FaTrophy,
  FaUserNinja,
  FaHatWizard,
  FaHandSparkles,
  FaCrown,
} from "react-icons/fa";
import { GiPartyPopper } from "react-icons/gi";
import { BiTrendingUp } from "react-icons/bi";
import { PerformanceBadgesColor, PerformanceBadgesType } from "./constants";

import performanceBadgeStyles from "./styles";
import { FiveStars } from "components/Icons/FiveStars";

interface PerformanceBadgeType {
  badgeName: PerformanceBadgesType;
  badgeColor: PerformanceBadgesColor;
}

export default function PerformanceBadge({
  badgeColor,
  badgeName,
}: PerformanceBadgeType) {
  const classes = performanceBadgeStyles();

  const icon = useMemo(() => {
    switch (badgeName) {
      case PerformanceBadgesType.CHIEF_CHALLENGER:
        return <FaCandyCane />;
      case PerformanceBadgesType.GAME_GURU:
        return <FaGamepad />;
      case PerformanceBadgesType.GOAL_NINJA:
        return <FaUserNinja />;
      case PerformanceBadgesType.GOOGLE_5STAR:
        return <FiveStars />;
      case PerformanceBadgesType.KPI_KINGPIN:
        return <FaBowlingBall />;
      case PerformanceBadgesType.LEVEL_MASTER:
        return <FaLevelUpAlt />;
      case PerformanceBadgesType.POINT_LEADER:
        return <FaStar />;
      case PerformanceBadgesType.RANKED_ONE:
        return <FaTrophy />;
      case PerformanceBadgesType.SCHEDULE_ADHERENCE:
        return <FaCalendarCheck />;
      case PerformanceBadgesType.SOCIAL_BUTTERFLY:
        return <FaBug />;
      case PerformanceBadgesType.TRENDING_STREAK:
        return <BiTrendingUp />;
      case PerformanceBadgesType.WINNING_WARRIOR:
        return <FaAward />;
      case PerformanceBadgesType.HIGH_5:
        return <FaHandSparkles />;
      case PerformanceBadgesType.SOCIAL_SULTAN:
        return <FaCrown />;
      case PerformanceBadgesType.SOCIALITE:
        return <GiPartyPopper />;
      case PerformanceBadgesType.WIZARD:
        return <FaHatWizard />;
    }
  }, [badgeName]);

  return (
    <Tooltip title={badgeName} arrow placement="top">
      <Box
        className={clsx(classes.performanceBadge, {
          [classes.bronze]: badgeColor === PerformanceBadgesColor.BRONZE,
          [classes.silver]: badgeColor === PerformanceBadgesColor.SILVER,
          [classes.gold]: badgeColor === PerformanceBadgesColor.GOLD,
          [classes.platinum]: badgeColor === PerformanceBadgesColor.PLATINUM,
          [classes.diamond]: badgeColor === PerformanceBadgesColor.DIAMOND,
          [classes.black]: badgeColor === PerformanceBadgesColor.BLACK,
          [classes.pink]: badgeColor === PerformanceBadgesColor.PINK,
        })}
      >
        <Box
          className={clsx(classes.performanceBadgeInner, {
            [classes.bronzeInner]: badgeColor === PerformanceBadgesColor.BRONZE,
            [classes.silverInner]: badgeColor === PerformanceBadgesColor.SILVER,
            [classes.goldInner]: badgeColor === PerformanceBadgesColor.GOLD,
            [classes.platinumInner]:
              badgeColor === PerformanceBadgesColor.PLATINUM,
            [classes.diamondInner]:
              badgeColor === PerformanceBadgesColor.DIAMOND,
            [classes.blackInner]: badgeColor === PerformanceBadgesColor.BLACK,
            [classes.pinkInner]: badgeColor === PerformanceBadgesColor.PINK,
          })}
        >
          <Box className={classes.performanceBadgeIcon}>{icon}</Box>
          <Box className={classes.performanceBadgeLabel}>
            {badgeColor === PerformanceBadgesColor.PINK
              ? badgeName
              : badgeColor}
          </Box>
        </Box>
      </Box>
    </Tooltip>
  );
}
