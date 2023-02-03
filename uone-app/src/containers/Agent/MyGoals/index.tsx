import React from "react";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Avatar from "../../../components/Avatar";
import FreeBreakfastIcon from "@material-ui/icons/FreeBreakfast";
import HourglassFullIcon from "@material-ui/icons/HourglassFull";
import TimerIcon from "@material-ui/icons/Timer";
import PhoneIcon from "@material-ui/icons/Phone";
import WcIcon from "@material-ui/icons/Wc";
import MyLocationIcon from "@material-ui/icons/MyLocation";
import StyledAvatar from "components/StyledAvatar";

const goals = [
  {
    title: "BR",
    icon: FreeBreakfastIcon,
    backgroundColor: "#66D7F9",
    value: Math.random(),
  },
  {
    title: "WT",
    icon: HourglassFullIcon,
    backgroundColor: "#B7E34B",
    value: Math.random(),
  },
  {
    title: "RT",
    icon: TimerIcon,
    backgroundColor: "#F25BA4",
    value: Math.random(),
  },
  {
    title: "BB",
    icon: WcIcon,
    backgroundColor: "#944EDC",
    value: Math.random(),
  },
  {
    title: "RC",
    icon: PhoneIcon,
    backgroundColor: "#FCD248",
    value: Math.random(),
  },
  {
    title: "FCR",
    icon: MyLocationIcon,
    backgroundColor: "#4FD1C7",
    value: Math.random(),
  },
];

export const ContainerMyGoals: React.FC = (props) => {
  return (
    <Grid container direction="row">
      <Paper>
        <Grid container direction="row">
          {goals.map((goal, index) => (
            <Grid
              key={index}
              item
              xs={4}
              style={{ paddingLeft: `calc(20% * ${goal.value})` }}
            >
              <h3>{goal.title}</h3>
              <StyledAvatar background={goal.backgroundColor}>
                <goal.icon />
              </StyledAvatar>
            </Grid>
          ))}
        </Grid>
      </Paper>
    </Grid>
  );
};
