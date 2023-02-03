import { Indicator } from "hooks/useIndicators";
import { Goal } from "components/GoalCard/types";

export interface GoalCreateProps {
  indicator: Indicator | undefined;
  departmentId: string;
  open: boolean;
  onClose: () => void;
  createGoalCallback: () => void;
}

export default GoalCreateProps;
