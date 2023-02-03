import { Indicator } from "hooks/useIndicators";
import { Goal } from "components/GoalCard/types";

export interface GoalEditProps {
  goal: Goal;
  onClose: () => void;
  onRefresh: () => void;
}

export default GoalEditProps;
