import { SharedSettings } from "containers";
import { useLayoutContext } from "layouts/LayoutProvider";
import { useEffect } from "react";

const Settings = () => {
  const { setLayoutTitle } = useLayoutContext();

  useEffect(() => {
    setLayoutTitle("Settings");
  }, [setLayoutTitle]);

  return <SharedSettings />;
};

export default Settings;
