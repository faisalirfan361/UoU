interface ImageUploadProps {
  callback(): void;
  type: string;
  handleLogoChange?(logo: any): void;
}

export default ImageUploadProps;
