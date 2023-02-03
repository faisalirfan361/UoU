interface Indicator {
  [key: string]: any;
}

interface SelectIndicatorModalProps {
  open: boolean;
  onClose: () => void;
  onRefresh: () => void;
  departmentId: string;
}

export default SelectIndicatorModalProps;
