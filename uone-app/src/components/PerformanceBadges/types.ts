enum BadgeColors {
  bronze = "bronze",
  silver = "silver",
  gold = "gold",
  platinum = "platinum",
  diamond = "diamond",
  black = "black",
}

enum BadgeTypes {
  CHIEF_CHALLENGER = "Chief Challenger",
  GAME_GURU = "Game Guru",
  GOAL_NINJA = "Goal Ninja",
  GOOGLE_5_STAR = "Google 5 Star",
  KPI_KINGPIN = "KPI Kingpin",
  LEVEL_MASTER = "Level Master",
  POINT_LEADER = "Point Leader",
  RANKED_NO1 = "#1 Ranked",
  SCHEDULE_ADHERENCE = "Schedule Adherence",
  SOCIAL_BUTTERFLY = "Social Butterfly",
  TRENDING_STREAK = "Trending Streak",
  WINNING_STREAK = "Winning Streak",
}

interface BadgeType {
  color: BadgeColors;
  type: BadgeTypes;
}

export interface PerformanceBadgesProps {
  badges: BadgeType[];
  minHeight?: string | number;
}
