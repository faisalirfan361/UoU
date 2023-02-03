export interface IProps {
  imageUrl?: string;
  title?: string;
  description?: string;
  points: number;
  canRedeem: boolean;
  reachedDailyPoints: boolean;
  onClick: () => void;
}

export default IProps;
