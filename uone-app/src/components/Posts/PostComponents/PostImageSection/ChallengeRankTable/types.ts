export interface User {
  user_id: number;
  firstName: string;
  lastName: string;
  profileImg: string;
  isWinner: boolean;
  [key: string]: any;
}

export interface ChallengePostImgProps {
  users: User[];
  challengeId: number;
}
