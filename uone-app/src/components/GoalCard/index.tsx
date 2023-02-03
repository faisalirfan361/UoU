import React, { useState, FC } from "react";
import { Box, Grid, Typography, Tooltip } from "@material-ui/core";
import { FaPen } from "react-icons/fa";
import { Can } from "context/Ability/Can";
import GoalCardProps from "./types";
import useGoalCardStyles from "./styles";
import { Indicator } from "hooks/useIndicators";
import GoalSlider from "components/GoalSlider";
import GoalEdit from "components/GoalForm/GoalEdit";
import RHFIOSSwitchComponent from "components/RHFSwitchIOS";
import { API } from "aws-amplify";
import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
import { useSnackbar } from "notistack";

const GoalCard: FC<GoalCardProps> = ({ goal, onRefresh }) => {
  const classes = useGoalCardStyles();
  const [isEdit, setIsEdit] = useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const [goalStatus, setGoalStatus] = useState<boolean>(
    typeof goal.attributes.status === "undefined"
      ? true
      : goal.attributes.status
  );
  const [disable, setDisable] = useState(false);

  const onSubmit = async () => {
    setDisable(true);
    let data = {
      type: "goal",
      entityId: goal.entityId,
      clientId: goal.clientId,
      groupId: goal.groupId,
      attributes: {
        ...goal.attributes,
        status: !goalStatus,
      },
    };
    setGoalStatus(!goalStatus);
    try {
      await API.post(config.apiGateway.NAME, "/entity/update", {
        body: data,
      });
      enqueueSnackbar("Goal edited successfully", SUCCESS_TOAST_OPTIONS);
    } catch (e) {
      enqueueSnackbar("Failed to edit Goal", ERROR_TOAST_OPTIONS);
    }
    onRefresh();
    setDisable(false);
  };
  return (
    <>
      {!isEdit ? (
        <Box className={classes.card} style={{ opacity: goalStatus ? 1 : 0.5 }}>
          <Grid container direction="row" className={classes.cardTop}>
            <Grid item xs={6}>
              <span className={classes.cardTopBtn}>
                <RHFIOSSwitchComponent
                  disabled={disable}
                  name="disable-status"
                  value={goalStatus}
                  onChange={() => onSubmit()}
                />
              </span>
              <span className={classes.cardTopLabel}>
                {goal.attributes.goalName}
              </span>
              {/*goal &&
              goal.attributes.indicators &&
              goal.attributes.indicators.length > 0
                ? goal.attributes.indicators.map(
                    (indicator: Indicator, index: number) => {
                      return (
                        <Tooltip
                          classes={{
                            tooltip: classes.tooltip,
                            popper: classes.popper,
                          }}
                          title={indicator.formula}
                          placement="bottom"
                        >
                          <span className={classes.labelTxt}>
                            {indicator.attributes.name}
                          </span>
                        </Tooltip>
                      );
                    }
                  )
                  : null*/}
            </Grid>
            <Grid item xs={6}>
              <div className={classes.topLeftContainer}>
                <div>
                  <span className={classes.labelTxt}>Type</span>
                  <p className={classes.cardTopLabel}>
                    {goal.attributes.metricType}
                  </p>
                </div>
                <div>
                  <span className={classes.labelTxt}>Duration</span>
                  <p className={classes.cardTopLabel}>
                    {goal.attributes.metricDuration}
                  </p>
                </div>
                <div>
                  <span className={classes.labelTxt}>Weighting</span>
                  <p
                    className={classes.cardTopLabel}
                  >{`${goal.attributes.weight}/100`}</p>
                </div>
                <div>
                  <span className={classes.labelTxt}>Points</span>
                  <p className={classes.cardTopLabel}>
                    {goal.attributes.points}
                  </p>
                </div>
                <div>
                  <Can I="edit" a="goals">
                    <div
                      onClick={() => setIsEdit(true)}
                      className={classes.penCircle}
                    >
                      <FaPen className={classes.pen} />
                    </div>
                  </Can>
                </div>
              </div>
            </Grid>
          </Grid>

          <Grid
            container
            className={classes.midGoalContainer}
            direction={goal.attributes.flip ? "row-reverse" : "row"}
          >
            <Grid item>
              <Typography component="span" className={classes.minGoalNumber}>
                MINIMUM NUMBER
              </Typography>

              <Typography component="p">
                {goal.attributes.minInfinite ? "∞" : goal.attributes.minNumber}
              </Typography>
            </Grid>

            <Grid item>
              <Typography component="span">GOAL:</Typography>
              <Typography component="span">
                {goal.attributes.goalValue}
              </Typography>
            </Grid>

            <Grid item className={classes.maxGoalNumber}>
              <Typography component="span">MAXIMUM NUMBER</Typography>
              <Typography component="p">
                {goal.attributes.maxInfinite ? "∞" : goal.attributes.maxNumber}
              </Typography>
            </Grid>
          </Grid>
          <Grid item xs={12} className={classes.multiSliderContainer}>
            <GoalSlider
              allowChange={false}
              minNumber={goal.attributes.minNumber}
              maxNumber={goal.attributes.maxNumber}
              goalValue={goal.attributes.goalValue}
              setGoalVal={(val: number) => {}}
              minInfinite={goal.attributes.minInfinite}
              maxInfinite={goal.attributes.maxInfinite}
              flipRange={goal.attributes.flip}
            />
          </Grid>
        </Box>
      ) : (
        <GoalEdit
          key={`goal-edit-${goal.entityId}`}
          goal={goal}
          onClose={() => setIsEdit(false)}
          onRefresh={onRefresh}
        />
      )}
    </>
  );
};

export default GoalCard;
