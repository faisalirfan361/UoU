export enum MeterStatus {
  BEHIND,
  ON_TRACK,
  MET,
}

export default interface GoalMeterProps {
  imageUrl?: string;
  meterTitle?: string;
  meterIcon?: any;
  meterStatus?: MeterStatus;
  meterGoal: number;
  meterPosition: number;
  startDate: Date;
  endDate: Date;
  value: number;
  confetti?: boolean;
  meterValue?: any;
  meterKpiId?: string;
  meterDuration: string;
  meterCoins?: string;
  flip?: boolean;
}
