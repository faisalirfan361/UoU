export interface GoalSliderProps {
  allowChange: boolean;
  minNumber: number;
  maxNumber: number;
  goalValue: number;
  setGoalVal: (val: number) => void;
  minInfinite: boolean;
  maxInfinite: boolean;
  flipRange: boolean;
}
