export interface ChallengeTitleProps {
  challengeId: number;
  challengeName: string;
  coins: number;
  startDate: Date;
  endDate: Date;
  editFunction?: (challengeId: any) => void;
  deleteFunction?: (challengeId: any) => void;
}
