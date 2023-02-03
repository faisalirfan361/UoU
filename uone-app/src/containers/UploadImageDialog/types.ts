export interface UploadImageDialogProps {
  open?: boolean;
  onClose?(): void;
  title?: string;
  type?: string;
  handleLogoChange?(logo: any): void;
}
