export interface AcceptCancelModalProps {
  isOpen: boolean;
  title: string;
  text: string;
  acceptFunc(): void;
  cancelFunc(): void;
  acceptText?: string;
  cancelText?: string;
  disabled?: boolean;
}

export default AcceptCancelModalProps;
