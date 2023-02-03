import { Indicator } from "../../hooks/useIndicators";

interface CreateIndicatorModalProps {
  open: boolean;
  onClose(): void;
  callback(): void;
  departmentId: string;
}

export default CreateIndicatorModalProps;
