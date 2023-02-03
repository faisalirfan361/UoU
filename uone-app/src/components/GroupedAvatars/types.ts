interface Image {
  alt: string,
  imgSrc: string,
  cssClass?: string,
};

interface IProps {
  images: Image[];
}

export default IProps;
