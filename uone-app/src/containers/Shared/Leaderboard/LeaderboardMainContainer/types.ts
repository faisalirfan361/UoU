import { Department } from "hooks/useDepartments";

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  performance: string;
}

export interface LeaderboardContainerProps {
  department?: Department;
  scrollableElementId?: string;
}
