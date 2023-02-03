export interface User {
  user_id: number;
  firstName: string;
  lastName: string;
  profileImg: string;
  isWinner: boolean;
  [key: string]: any;
}

export interface ChallengeCardProps {
  challenge: any;
  editFunction?: (challengeId: number) => void;
  deleteFunction?: (challengeId: number) => void;
}
