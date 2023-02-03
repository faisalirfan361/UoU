import { useLayoutContext } from "layouts/LayoutProvider";
import { useEffect } from "react";

const Importer = () => {
  const { setLayoutTitle } = useLayoutContext();

  useEffect(() => {
    setLayoutTitle("Importer");
  }, [setLayoutTitle]);
  return <h1>Importer</h1>;
};

export default Importer;
