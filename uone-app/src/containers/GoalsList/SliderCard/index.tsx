import { Box, Grid, Typography, Checkbox } from "@material-ui/core";
import React, { useState, FC } from "react";
import SliderCardProps from "./types";
import useSliderCardStyle from "./styles";
import { RHFInputComponent } from "components";
import SliderCardTop from "../SliderCardTop";
import { useForm, useFieldArray } from "react-hook-form";
import { FaPen, FaSync } from "react-icons/fa";
import MultiSlider from "multi-slider";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import CheckBoxIcon from "@material-ui/icons/CheckBox";

const SliderCard: FC<SliderCardProps> = ({
  index,
  kpi_id,
  kpi_name,
  currentMetric,
  currentDuration,
  handleMetricChange,
  handleDuraChange,
  currentGoal,
  goalMin,
  goalMax,
  handleFieldChange,
  newGoal,
  setNewGoal,
}) => {
  const { control } = useForm({});
  const [redAreaVal, setRedAreaVal] = useState([20]);
  const [yAreaVal, setYAreaVal] = useState([currentGoal]);
  const [gAreaVal, setGAreaVal] = useState([60]);
  const [goal, setGoals] = useState([40, 20, 60]);
  const [isEdit, setIsEdit] = useState(newGoal ? true : false);
  const [mininfinite, setMininfinite] = useState(false);
  const [maxinfinite, setMaxinfinite] = useState(false);
  const [flipRange, setFlipRange] = useState(false);

  const classes = useSliderCardStyle();

  const onChangeSlide = (val: any, index = 0) => {
    var gol = [...goal];
    if (val[0] != redAreaVal[index]) {
      if (gol[index] == 0 && val[0] < redAreaVal[index]) {
        gAreaVal[index] = gAreaVal[index];
        yAreaVal[index] = yAreaVal[index];
        redAreaVal[index] = redAreaVal[index];
      } else {
        if (val[2] == 0) {
          gAreaVal[index] = 30;
          yAreaVal[index] = 50;
          redAreaVal[index] = 40;
          gol[index] += 1;
        } else if (val[0] == 0 && gol[index] != 0) {
          gAreaVal[index] = 30;
          yAreaVal[index] = 50;
          redAreaVal[index] = 40;
          gol[index] -= 1;
        } else if (val[0] < redAreaVal[index]) {
          gAreaVal[index] = val[2] + 1;
          yAreaVal[index] = 50;
          gol[index] -= 1;
          redAreaVal[index] = val[0];
        } else {
          gAreaVal[index] = val[2] - 1;
          yAreaVal[index] = 50;
          gol[index] += 1;
          redAreaVal[index] = val[0];
        }
      }
      setRedAreaVal([...redAreaVal]);
      setGAreaVal([...gAreaVal]);
      setYAreaVal([...yAreaVal]);
      setGoals(gol);
      {
        !newGoal && handleFieldChange("goal", yAreaVal[index], kpi_id);
      }
    } else {
      if (gol[index] == 0 && val[2] > gAreaVal[index]) {
        gAreaVal[index] = gAreaVal[index];
        yAreaVal[index] = yAreaVal[index];
        redAreaVal[index] = redAreaVal[index];
      } else {
        if (val[2] == 0) {
          gAreaVal[index] = 30;
          yAreaVal[index] = 50;
          redAreaVal[index] = 40;
          gol[index] += 1;
        } else if (val[0] == 0 && gol[index] != 0) {
          gAreaVal[index] = 30;
          yAreaVal[index] = 50;
          redAreaVal[index] = 40;
          gol[index] -= 1;
        } else if (val[2] < gAreaVal[index]) {
          redAreaVal[index] = val[0] + 1;
          gAreaVal[index] = val[2];
          yAreaVal[index] = 50;
          gol[index] += 1;
        } else {
          redAreaVal[index] = val[0] - 1;
          gol[index] -= 1;
          gAreaVal[index] = val[2];
          yAreaVal[index] = 50;
        }
      }
      setRedAreaVal([...redAreaVal]);
      setGAreaVal([...gAreaVal]);
      setYAreaVal([...yAreaVal]);
      setGoals(gol);
      {
        !newGoal && handleFieldChange("goal", yAreaVal[index], kpi_id);
      }
    }
  };

  return (
    <Box className={newGoal ? classes.newGoalCard : classes.card}>
      <SliderCardTop
        kpi_name={kpi_name}
        handleFieldChange={handleFieldChange}
        newGoal={newGoal}
        setNewGoal={setNewGoal}
        isEdit={isEdit}
        setIsEdit={setIsEdit}
        currentMetric={currentMetric}
        currentDuration={currentDuration}
        currentGoal={currentGoal}
      />
      {isEdit && (
        <Grid
          container
          direction={flipRange ? "row-reverse" : "row"}
          className={classes.midInputContainer}
        >
          <Grid item xs={2}>
            <RHFInputComponent
              control={control}
              name="minNumber"
              defaultValue={goalMin}
              variant="outlined"
              label="Min Number"
              type="number"
            />
            <FormControlLabel
              control={
                <Checkbox
                  icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                  checkedIcon={<CheckBoxIcon fontSize="small" />}
                  name="mininfinite"
                  onChange={(e) => {
                    setMininfinite(e.target.checked);
                  }}
                />
              }
              label="infinite"
            />
          </Grid>
          <Grid item xs={2}>
            <RHFInputComponent
              control={control}
              name="goal"
              defaultValue={currentGoal}
              variant="outlined"
              label="Goal"
              type="number"
            />
          </Grid>
          <Grid item xs={2}>
            <RHFInputComponent
              control={control}
              name="maxNumber"
              defaultValue={goalMax}
              variant="outlined"
              label="Max Number"
              type="number"
            />
            <FormControlLabel
              control={
                <Checkbox
                  icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                  checkedIcon={<CheckBoxIcon fontSize="small" />}
                  name="maxinfinite"
                  onChange={(e) => {
                    setMaxinfinite(e.target.checked);
                  }}
                />
              }
              label="infinite"
            />
          </Grid>
        </Grid>
      )}
      <Grid
        container
        className={classes.midGoalContainer}
        direction={flipRange ? "row-reverse" : "row"}
      >
        <Grid item>
          {!isEdit && (
            <Typography component="span" className={classes.minGoalNumber}>
              MINIMUM NUMBER
            </Typography>
          )}
          <Typography component="p">{mininfinite ? "∞" : goalMin}</Typography>
        </Grid>

        <Grid item>
          <Typography component="span">GOAL:</Typography>
          <Typography component="span">{currentGoal}</Typography>
        </Grid>

        <Grid className={classes.maxGoalNumber}>
          {!isEdit && <Typography component="span">MAXIMUM NUMBER</Typography>}
          <Typography component="p">{maxinfinite ? "∞" : goalMax}</Typography>
        </Grid>
      </Grid>
      <Grid item xs={12} className={classes.multiSliderContainer}>
        <MultiSlider
          values={[redAreaVal[0], yAreaVal[0], gAreaVal[0]]}
          onChange={(val: any) => onChangeSlide(val, 0)}
          trackSize={4}
          padX={0}
          handleStrokeSize={1}
          handleInnerDotSize={0}
          handleSize={4}
          height={10}
          colors={["#E95AA4", "#FBD246", "#B8E34A"]}
          className={classes.rangeSlider}
        />
      </Grid>
      {isEdit && (
        <Grid container direction="row" xs={12} className={classes.flipWrapper}>
          <Box className={classes.flipBox}>
            <span className={classes.flipTxt}>Flip Range</span>
            <FaSync
              className={classes.flipIcon}
              onClick={() => setFlipRange(!flipRange)}
            />
          </Box>
        </Grid>
      )}
    </Box>
  );
};

export default SliderCard;
