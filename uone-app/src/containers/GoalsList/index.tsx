import { memo, useState, useEffect } from "react";
import { useRecoilValue } from "recoil";
import { API } from "aws-amplify";
import _get from "lodash.get";
import _differenceWith from "lodash.differencewith";
import _isEqual from "lodash.isequal";
import _debounce from "lodash.debounce";
import { Grid, Box } from "@material-ui/core";
import { create, all } from "mathjs";

import { userAtom } from "state";
import config from "config";
import { useDepartments } from "../../hooks/useDepartments";
import { useStyle } from "./styles";
import DepartmentSelector from "components/DepartmentSelector";
import { ButtonActionComponent } from "components";
import NoGoalSet from "./NoGoalSet";
import GoalCard from "components/GoalCard";
import GoalCardSkeleton from "components/GoalCard/GoalCardSkeleton";
import SelectIndicatorModal from "components/SelectIndicatorModal";
import { Goal } from "components/GoalCard/types";
import { Can } from "context/Ability/Can";

const GoalsList = (props: any) => {
  const classes = useStyle();
  const { departmentId } = useRecoilValue(userAtom);
  const [goals, setGoals] = useState<Goal[]>(new Array(10).fill(null));

  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();

  const [selectIndicatorModalOpen, setSelectIndicatorModalOpen] =
    useState(false);

  useEffect(() => {
    getGoals();
    testMath();
  }, []);

  useEffect(() => {
    getGoals();
  }, [selectedDepartment]);

  const getGoals = async () => {
    let path = "/entity/get-goals";

    if (selectedDepartment && selectedDepartment.department_id != "-1") {
      path += `?department=${selectedDepartment.department_id}`;
    }

    const data = await API.get(config.apiGateway.NAME, path, {});
    setGoals(data);
  };

  const getDepartmentId = (): string => {
    return selectedDepartment.department_id === "-1"
      ? departmentId
      : selectedDepartment.department_id;
  };

  const testMath = () => {
    //https://mathjs.org/docs/expressions/expression_trees.html
    const config = {};
    const math = create(all, config);
    const expression =
      "(Talk_Time+total_hold_time++total_Wrap_time)/No_Calls_Handle"; //"(sum(metric)*3)/7*100";
    try {
      const node = math?.parse ? math.parse(expression) : "";
      console.log("NODE: ", node);
    } catch (e) {
      console.log(e);
    }
  };

  const openSelectIndicatorModal = () => {
    setSelectIndicatorModalOpen(true);
  };
  const closeSelectIndicatorModal = () => {
    setSelectIndicatorModalOpen(false);
  };

  return (
    <>
      <Box>
        <Grid container justifyContent="space-between" alignItems="center">
          <Grid item>
            <DepartmentSelector
              options={departments}
              defaultOption={defaultDepartment}
              onSelect={setSelectedDepartment}
            />
          </Grid>
          <Can I="create" a="goals">
            <Grid item>
              <ButtonActionComponent handleOnClick={openSelectIndicatorModal}>
                Create New Goal
              </ButtonActionComponent>
            </Grid>
          </Can>
        </Grid>
        <Box mt={2} className={classes.goalContainer}>
          {goals.length > 0 ? (
            goals.map((goal: Goal, index: number) => {
              return goal ? (
                <GoalCard
                  key={`goal-card-${index}`}
                  goal={goal}
                  onRefresh={getGoals}
                />
              ) : (
                <GoalCardSkeleton key={`goal-card-skeleton-${index}`} />
              );
            })
          ) : (
            <NoGoalSet onCreateGoalClick={openSelectIndicatorModal} />
          )}
        </Box>
      </Box>

      <SelectIndicatorModal
        onClose={closeSelectIndicatorModal}
        open={selectIndicatorModalOpen}
        onRefresh={getGoals}
        departmentId={getDepartmentId()}
      />
    </>
  );
};

export default memo(GoalsList);
