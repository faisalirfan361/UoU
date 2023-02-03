import { ChallengeType } from "components/GameCard/type";

export interface EditChallengeProps {
  refreshFunction?(gameId: string, challenge: ChallengeType): void;
  challenge: any;
}
