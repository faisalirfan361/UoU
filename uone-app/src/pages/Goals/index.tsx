import { GoalsList } from "containers";
import { useLayoutContext } from "layouts/LayoutProvider";
import { useEffect } from "react";

const Goals = () => {
  const { setLayoutTitle } = useLayoutContext();

  useEffect(() => {
    setLayoutTitle("Goals");
  }, [setLayoutTitle]);

  return <GoalsList />;
};

export default Goals;
