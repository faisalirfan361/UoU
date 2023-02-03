import { Card } from "@material-ui/core";
import { FC } from "react";

import GameCardContent from "./GameCardLayout/GameCardContent";
import GameCardHeader, {
  GameCardHeaderProps,
} from "./GameCardLayout/GameCardHeader";
import GameCardMedia, {
  GameCardMediaProps,
} from "./GameCardMedia/GameCardMedia";
import GameCardStat, { GameCardStatProps } from "./GameCardStats/GameCardStat";
import GameCardStats, {
  GameCardStatsProps,
} from "./GameCardStats/GameCardStats";
import GameCardTable, {
  GameCardTableProps,
} from "./GameCardTable/GameCardTable";

interface GameCardCompoundProps {
  GameCardHeader: FC<GameCardHeaderProps>;
  GameCardMedia: FC<GameCardMediaProps>;
  GameCardContent: FC;
  GameCardStats: FC<GameCardStatsProps>;
  GameCardStat: FC<GameCardStatProps>;
  GameCardTable: FC<GameCardTableProps>;
}

const GameCard: FC & GameCardCompoundProps = ({ children }) => (
  <Card style={{ width: "100%", margin: 8 }}>{children}</Card>
);

GameCard.GameCardHeader = GameCardHeader;
GameCard.GameCardMedia = GameCardMedia;
GameCard.GameCardContent = GameCardContent;
GameCard.GameCardStats = GameCardStats;
GameCard.GameCardStat = GameCardStat;
GameCard.GameCardTable = GameCardTable;

export default GameCard;
