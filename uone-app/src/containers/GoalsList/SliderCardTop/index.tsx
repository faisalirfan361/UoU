import {
  Box,
  Input,
  Grid,
  Typography,
  Button,
  Tooltip,
} from "@material-ui/core";
import downCaret from "../../../assets/img/misc/down-caret.png";
import useSliderCardTopStyle from "./styles";
import { RHFInputComponent } from "components";
import { useForm } from "react-hook-form";
import { FaPen } from "react-icons/fa";
import { useState, FC } from "react";
import IProps from "./types";
const defaultValues = {
  points: "1000",
};

const SliderCardTop: FC<IProps> = ({
  kpi_name,
  handleFieldChange,
  newGoal,
  setNewGoal,
  isEdit,
  setIsEdit,
  currentMetric,
  currentDuration,
  currentGoal,
}) => {
  const [kpiName, setKpiName] = useState(kpi_name);
  const [metric, setMetric] = useState(0);
  const [duration, setDuration] = useState(1);

  const DEFAULT_DURATION = [
    {
      value: 1,
      label: "Hourly",
    },
    {
      value: 2,
      label: "Weekly",
    },
    {
      value: 3,
      label: "Monthly",
    },
  ];
  const DEFAULT_METRIC = [
    {
      value: 1,
      label: "Time",
    },
    {
      value: 2,
      label: "Dollar",
    },
    {
      value: 3,
      label: "Number",
    },
    {
      value: 4,
      label: "Percent",
    },
  ];

  const handleMetricChange = (event: any) => {
    const value = event.target.value;
    setMetric(value - 1);
  };

  const handleDurationChange = (event: any) => {
    const value = event.target.value;
    setDuration(value);
  };

  const handleCancel = () => {
    setIsEdit(false);
    setNewGoal && setNewGoal();
  };
  const handleSave = () => {
    setIsEdit();
    setNewGoal && setNewGoal();
  };

  const classes = useSliderCardTopStyle();
  const { control, handleSubmit, errors, reset, watch, register } = useForm({
    defaultValues,
  });

  return (
    <form>
      <Grid container direction="row" xs={12} className={classes.cardTop}>
        {!newGoal && isEdit && (
          <Grid
            container
            direction="row"
            xs={12}
            className={classes.topButtonContainer}
          >
            <Box>
              <Typography
                component="span"
                className={classes.cancelButton}
                onClick={handleCancel}
              >
                Cancel
              </Typography>
              <Button onClick={handleSave} className={classes.outlinedPrimary}>
                Save edit
              </Button>
            </Box>
          </Grid>
        )}
        <Grid xs={6}>
          <span className={classes.labelTxt}>kpi</span>
          {!isEdit ? (
            //Just display
            <Tooltip
              classes={{
                tooltip: classes.tooltip,
                popper: classes.popper,
              }}
              title={kpiName}
              placement="bottom"
            >
              <p className={classes.cardTopLabel}>{kpiName} -kpi name</p>
            </Tooltip>
          ) : (
            //Edit name, but this needs to be a dropdown
            <Input
              value={kpiName}
              placeholder="set name"
              className={classes.kpiInput}
              onChange={(e) => setKpiName(e.target.value)}
            />
          )}
        </Grid>
        {!isEdit ? (
          //Display gal metric type, duration, weight and points
          <Grid xs={4}>
            <div className={classes.topLeftContainer}>
              <div>
                <span className={classes.labelTxt}>
                  {metric ? DEFAULT_METRIC[metric].label : currentMetric}
                </span>
                <p className={classes.cardTopLabel}>{currentGoal}</p>
              </div>
              <div>
                <span className={classes.labelTxt}>Durationn</span>
                <p className={classes.cardTopLabel}>
                  {DEFAULT_DURATION[duration - 1].label}
                </p>
              </div>
              <div>
                <span className={classes.labelTxt}>Weighting</span>
                <p className={classes.cardTopLabel}>25/100</p>
              </div>
              <div>
                <span className={classes.labelTxt}>Points</span>
                <p className={classes.cardTopLabel}>1000</p>
              </div>
              <div
                onClick={() => setIsEdit(true)}
                className={classes.penCircle}
              >
                <FaPen className={classes.pen} />
              </div>
            </div>
          </Grid>
        ) : (
          //Edit goal
          <Grid xs={6}>
            <div className={classes.topLeftContainer}>
              <div className={classes.metricWraper}>
                <p className={classes.labelTxt}>Metric</p>
                <select
                  name="dept"
                  defaultValue={currentMetric}
                  className={classes.deptSelect}
                  onChange={handleMetricChange}
                >
                  {DEFAULT_METRIC.map((o, i) => (
                    <option value={o.value} key={i}>
                      {o.label}
                    </option>
                  ))}
                </select>
                <span>
                  <img src={downCaret} alt="down-caret" />
                </span>
              </div>
              <div className={classes.metricWraper}>
                <p className={classes.labelTxt}>Duration</p>
                <select
                  name="dept"
                  defaultValue={currentDuration}
                  className={classes.deptSelect}
                  onChange={handleDurationChange}
                >
                  {DEFAULT_DURATION.map((o, i) => (
                    <option value={o.value} key={i}>
                      {o.label}
                    </option>
                  ))}
                </select>
                <span>
                  <img src={downCaret} alt="down-caret" />
                </span>
              </div>
              <div className={classes.metricWraper}>
                <p className={classes.labelTxt}>WEIGHTING</p>
                <span className={classes.labelTxtValue}>25/100</span>
              </div>
              <div style={{ width: "30%" }}>
                <RHFInputComponent
                  control={control}
                  name="points"
                  defaultValue="points"
                  variant="outlined"
                  label="Points"
                  type="number"
                />
              </div>
            </div>
          </Grid>
        )}
      </Grid>
    </form>
  );
};

export default SliderCardTop;
