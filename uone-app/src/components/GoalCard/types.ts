import { Indicator } from "hooks/useIndicators";
interface GoalAttributes {
  goalName: string;
  metricType: string;
  goalValue: number;
  minNumber: number;
  maxNumber: number;
  minInfinite: boolean;
  clientId: string;
  departmentId: string;
  weight: number;
  maxInfinite: boolean;
  flip: boolean;
  metricDuration: string;
  points: number;
  indicator: string;
  status?: boolean;
}

export interface Goal {
  created_at: string;
  departmentId: string;
  entityId: string;
  clientId: string;
  type: string;
  groupId: string;
  attributes: GoalAttributes;
}

export interface GoalCardProps {
  goal: Goal;
  onRefresh: () => void;
}

export default GoalCardProps;
