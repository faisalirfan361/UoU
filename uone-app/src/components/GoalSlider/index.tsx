import { useState, FC, useEffect } from "react";
import { GoalSliderProps } from "./types";
import useGoalSliderStyles from "./styles";
import MultiSlider from "multi-slider";

const GoalSlider: FC<GoalSliderProps> = ({
  allowChange,
  minNumber,
  maxNumber,
  goalValue,
  setGoalVal,
  minInfinite,
  maxInfinite,
  flipRange,
}) => {
  const classes = useGoalSliderStyles();
  const [internalGoal, setInternalGoal] = useState(-1);

  //red = distance between point zero and red ball
  //distance = distance between red ball and green
  //green = distance between green and point 100
  const [sliderValues, setSliderValues] = useState([1, 1, 1]);

  useEffect(() => {
    setInitialSliderValues();
  }, []);

  useEffect(() => {
    if (goalValue != internalGoal) {
      setInitialSliderValues();
    }
  }, [goalValue]);

  useEffect(() => {
    setInitialSliderValues();
  }, [minInfinite, maxInfinite, allowChange, minNumber, maxNumber, flipRange]);

  const setInitialSliderValues = () => {
    if (!minInfinite && !maxInfinite) {
      //translated the goal to the percentage between minNumber and maxNumber
      let transformed =
        ((goalValue - minNumber) / (maxNumber - minNumber)) * 100;

      //Calculate values to set the slider bar
      const green = 100 - transformed;
      const distance = transformed * 0.2;
      const red = 100 - (green + distance);
      if (!flipRange) {
        setSliderValues([red, distance, green]);
      } else {
        const flipedGreen = 100 - green;
        const flipedDistance = green * 0.2;
        const flipRed = green - flipedDistance;
        setSliderValues([flipRed, flipedDistance, flipedGreen]);
      }
      setInternalGoal(transformed);
    } else {
      //using infinite max or min
      setSliderValues([40, 20, 60]);
    }
  };

  const onChangeSlide = (val: any, index = 0) => {
    //slider can be use if allowChange if false, or infinite for max or mix is set to true
    if (!allowChange || minInfinite || maxInfinite) return null;

    let goalVal = 0;
    let greenTemp = 0;
    let distanceTemp = 0;
    let redTemp = 0;
    if (val[2] != sliderValues[2]) {
      //green ball was moved
      goalVal = 100 - val[2];
      greenTemp = 100 - goalVal;
      distanceTemp = goalVal * 0.2;
      redTemp = 100 - (greenTemp + distanceTemp);
    } else {
      //red ball was moved
      redTemp = val[0];
      goalVal = redTemp / 0.8;
      distanceTemp = goalVal - redTemp;
      greenTemp = 100 - goalVal;
    }
    setSliderValues([redTemp, distanceTemp, greenTemp]);
    let transform =
      (goalVal / 100) * (maxNumber - minNumber) +
      (maxNumber - (maxNumber - minNumber));
    if (flipRange) {
      transform = maxNumber - transform + (maxNumber - (maxNumber - minNumber));
    }
    transform = Math.round(transform);
    setInternalGoal(transform);
    setGoalVal(transform);
  };

  const areValuesValid = (): boolean => {
    if (!maxNumber || maxNumber == 0 || !goalValue || goalValue <= 0) {
      return false;
    }
    return true;
  };

  return (
    <>
      {areValuesValid() ? (
        <MultiSlider
          values={sliderValues}
          onChange={(val: any) => onChangeSlide(val, 0)}
          trackSize={4}
          padX={0}
          handleStrokeSize={1}
          handleInnerDotSize={0}
          handleSize={4}
          height={10}
          colors={["#E95AA4", "#FBD246", "#B8E34A"]}
        />
      ) : (
        ""
      )}
    </>
  );
};

export default GoalSlider;
