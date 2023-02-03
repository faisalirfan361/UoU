import React, { useState, useMemo, useEffect } from "react";
import { Grid, Box } from "@material-ui/core";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import { useMetrics, Metric } from "hooks/useMetrics";

import MetricsSelector from "components/MetricsSelector";
import { RHFInputComponent } from "components";
import useIndicatorFormulaCreatorStyle from "./style";
import IndicatorFormulaCreatorProps, { MetricPosition } from "./types";

/*Returns the string index positions for all the metrics used in formula*/
const createMetricStringPositions = (formula: string) => {
  const positions: MetricPosition[] = [];

  for (let i = 0; i < formula.length; i++) {
    const currentChar = formula[i];

    if (currentChar == "{") {
      for (let e = i + 1; e < formula.length; e++) {
        const endChar = formula[e];
        if (endChar == "}") {
          positions.push({ start: i, end: e });
          i = e;
          break;
        }
      }
    }
  }
  return positions;
};

const isPositionInMetric = (
  positionIndex: number,
  positions: MetricPosition[]
): boolean => {
  for (const position of positions) {
    if (positionIndex > position.start && positionIndex <= position.end) {
      return true;
    }
  }
  return false;
};

/* Checks if the delete the user is trying to do should delete a metric*/
const isDeleteAMetric = (
  positionIndex: number,
  positions: MetricPosition[]
): boolean => {
  for (const position of positions) {
    if (positionIndex == position.end) {
      return true;
    }
  }
  return false;
};

/*Gets the metric position we want to delete */
const getMetricToDelete = (
  positionIndex: number,
  positions: MetricPosition[]
): MetricPosition | null => {
  for (const position of positions) {
    if (positionIndex == position.end) {
      return position;
    }
  }
  return null;
};

const extractString = (startIndex: number, endIndex: number, text: string) => {
  let newString = "";
  for (let i = 0; i <= text.length - 1; i++) {
    if (i < startIndex || i > endIndex) {
      newString += text[i];
    }
  }
  return newString;
};

const IndicatorFormulaCreator: React.FC<IndicatorFormulaCreatorProps> = ({
  setFormula,
  control,
  errors,
  setValue,
}) => {
  const classes = useIndicatorFormulaCreatorStyle();
  const { clientName, departmentId } = useRecoilValue(userAtom);
  const [metricPositions, setMetricPositions] = useState<MetricPosition[]>([]);
  const [formulaString, setFormulaString] = useState("");
  const [deleteCaret, setDeleteCaret] = useState(0);
  const [addCaret, setAddCaret] = useState(-1);
  const [isDelete, setIsDelete] = useState(false);

  const metricsData = useMetrics(clientName);
  const metricsList: Metric[] = useMemo(() => {
    return metricsData && metricsData.metrics ? metricsData.metrics : [];
  }, [metricsData]);
  const [selectedMetric, setSelectedMetric] = useState<Metric>();

  useEffect(() => {
    //Every time formula changes, we generate the metric positions
    setMetricPositions(createMetricStringPositions(formulaString));
    const input: any = document.getElementById("formula-creator");
    if (input && isDelete) {
      //if formula changed due to deletion, we need to set the input caret
      //Otherwise caret will go to the end of the string
      input.focus();
      input.setSelectionRange(deleteCaret, deleteCaret);
      setIsDelete(false);
    }
    setFormula(formulaString);
    setValue("formula", formulaString);
  }, [formulaString]);

  //Input on change
  const handleOnChange = (e: any) => {
    const focusPosition = e.target.selectionStart - 1;
    //We validate that no text can be added inside a metric text
    if (!isPositionInMetric(focusPosition, metricPositions)) {
      const newChar = e.target.value[focusPosition];
      const lastChar = formulaString.charAt(formulaString.length - 1);
      //We validate that only the following characters
      //can be added to the formula: 0123456789 * / + - ()
      const newCondition =
        lastChar.match(`^[*.\/+-]+$`) == null ||
        newChar.match(`^[*.\/+-]+$`) == null ||
        lastChar == null;
      if (
        newChar &&
        newChar.match(`^[0-9() *.\/+-]+$`) != null &&
        newCondition
      ) {
        setFormulaString(e.target.value);
      } else if (e.target.value == "") {
        setFormulaString("");
      }
    }
  };

  const handleOnDelete = (e: any) => {
    //If a deletion process is in progress or a select text delete happens
    if (isDelete || e.target.selectionStart != e.target.selectionEnd) {
      e.preventDefault();
      return;
    }

    const focusPosition = e.target.selectionStart;
    if (e.keyCode === 8) {
      //backspace

      if (
        isPositionInMetric(focusPosition, metricPositions) ||
        focusPosition - 1 < 0
      ) {
        return e.preventDefault();
      }

      const positionAfterDelete = focusPosition - 1;

      if (isDeleteAMetric(positionAfterDelete, metricPositions)) {
        const metricToDelete = getMetricToDelete(
          positionAfterDelete,
          metricPositions
        );
        if (metricToDelete) {
          //we extract the metric string from the formula
          const newFormula = extractString(
            metricToDelete.start,
            metricToDelete.end,
            formulaString
          );
          //Set to true, so we set the caret position on useEffect
          setIsDelete(true);
          setDeleteCaret(metricToDelete.start);
          setFormulaString(newFormula);
          return e.preventDefault();
        }
      } else {
        //Delete is for just one char
        const newFormula = extractString(
          positionAfterDelete,
          positionAfterDelete,
          formulaString
        );
        setIsDelete(true);
        setDeleteCaret(positionAfterDelete);
        setFormulaString(newFormula);
        return e.preventDefault();
      }
    } else if (e.keyCode === 46) {
      //delete button
      return e.preventDefault();
    }
  };

  const onMouseUp = (e: any) => {
    setAddCaret(e.target.selectionStart);
  };

  const onKeyUp = (e: any) => {
    setAddCaret(e.target.selectionStart);
  };

  const cleanFormula = () => {
    setFormulaString("");
    setAddCaret(0);
    setIsDelete(false);
    setDeleteCaret(0);
  };

  const addMetricText = (metricString: string) => {
    if (metricString) {
      let newFormula = "";
      const lastChar = formulaString.charAt(formulaString.length - 1);
      if (
        !isPositionInMetric(addCaret, metricPositions) &&
        (lastChar.match(`^[0-9()*.\/+-]+$`) != null || formulaString.length < 1)
      ) {
        if (addCaret < formulaString.length) {
          newFormula = formulaString.substr(0, addCaret);
          newFormula += ` ${metricString} `;
          newFormula += formulaString.substr(addCaret, formulaString.length);
        } else {
          newFormula = `${formulaString} ${metricString} `;
        }

        setFormulaString(newFormula);
      }
    }
  };

  const addSum = () => {
    if (selectedMetric && selectedMetric.id != "-1") {
      addMetricText(`{ Sum(${selectedMetric.id}) }`);
    }
  };

  const addCount = () => {
    if (selectedMetric && selectedMetric.id != "-1") {
      addMetricText(`{ Count(${selectedMetric.id}) }`);
    }
  };

  const addAvg = () => {
    if (selectedMetric && selectedMetric.id != "-1") {
      addMetricText(`{ Avg(${selectedMetric.id}) }`);
    }
  };

  const addMetric = () => {
    if (selectedMetric && selectedMetric.id != "-1") {
      addMetricText(`{ ${selectedMetric.id} }`);
    }
  };

  return (
    <Box className={classes.root}>
      <Grid container alignItems="center" className={classes.gridContainer}>
        <Grid item xs={6}>
          <MetricsSelector
            options={metricsList}
            onSelect={setSelectedMetric}
            label="Select Indicator"
          />
        </Grid>
        <Grid
          item
          container
          xs={6}
          spacing={2}
          direction="row"
          justifyContent="center"
          alignItems="center"
        >
          <Grid item>
            <button
              type="button"
              className={classes.operandButton}
              onClick={addMetric}
            >
              Metric
            </button>
          </Grid>

          <Grid item>
            <button
              type="button"
              className={classes.operandButton}
              onClick={addSum}
            >
              Sum()
            </button>
          </Grid>
          <Grid item>
            <button
              type="button"
              className={classes.operandButton}
              onClick={addCount}
            >
              Count()
            </button>
          </Grid>
          <Grid item>
            <button
              type="button"
              className={classes.operandButton}
              onClick={addAvg}
            >
              Avg()
            </button>
          </Grid>
        </Grid>
      </Grid>
      <Grid container>
        <Grid item xs={12}>
          <RHFInputComponent
            control={control}
            name="formula"
            inputProps={{
              id: "formula-creator",
              onChange: handleOnChange,
              onKeyDown: handleOnDelete,
              onKeyUp: onKeyUp,
              onMouseUp: onMouseUp,
              value: formulaString,
              autoComplete: "off",
              className: classes.formulaInput,
            }}
            defaultValue={formulaString}
            variant="outlined"
            label="Formula"
            errors={errors}
          />
          <span className={classes.clearButton} onClick={cleanFormula}>
            Clean
          </span>
        </Grid>
      </Grid>
    </Box>
  );
};

export default IndicatorFormulaCreator;
