import { makeStyles } from "@material-ui/core/styles";

const Style = (props: any) =>
  makeStyles((theme) => ({
    // Challenge
    blocks: {
      display: "flex",
    },
    block: {
      minHeight: 148,
      width: "calc(50% + 5rem)",
    },
    blockLeft: {
      backgroundColor: "#999",
      backgroundImage: `url(${props.agent1})`,
      clipPath: "polygon(0 0, 100% 0, calc(100% - 3rem) 100%, 0% 100%)",
      backgroundSize: "cover",
    backgroundRepeat: "no-repeat",
    backgroundPosition: "center",
    },
    blockRight: {
      backgroundColor: "#666",
      backgroundImage: `url(${props.agent2})`,
      marginLeft: "-2.8rem",
      clipPath: "polygon(3rem 0, 100% 0, 100% 100%, 0% 100%)",
      backgroundSize: "100%",
    backgroundRepeat: "no-repeat",
    backgroundPosition: "center",
    },
    vsContainer: {
      position: "relative",
      minHeight: 148,
      width: "100%"
    },
    vsContainerA: {
      width: "100%",
      height: "100%",
      position: "absolute",
      top: 0,
      left: 0,
    },
    vsContainerB: {
      zIndex: 100,
      position: "inherit",
    },
    vsLeft: {
      minHeight: 148,
    },
    vsCenter: {
      minHeight: 148,
    },
    vsRight: {
      minHeight: 148,
    },
    vsCenterVSText: {
      background: "#FFFFFF",
      width: 37,
      height: 37,
      lineHeight: 2,
      borderRadius: "50%",
      border: "2px solid white",
      fontSize: 18,
      color: "#EF647B",
      weight: 500,
      textAlign: "center",
    },
    vsCenterVSAgents: {
      fontSize: 11,
      color: "#252525",
    },
    vsCenterVSTextChallenged: {
      color: "#252525",
    },
    vsCenterVSTextDetails: {
      fontSize: 12,
      color: "#000",
    },
    vsItemLeft: {
      width: "100%",
    },
    vsItemRight: {
      width: "100%",
    },
    vsItemCenter: {
      maxWidth: 48,
    },
    vsBottom: {
      borderColor: "#000",
      backgroundColor: "#F25BA4",
    },
    leftImage:{
      right: "13%",
      position: "relative",
      top: "-12px",
    },
    rightImage:{
      left: "13%",
      position: "relative",
      top: "12px",
    },
  }));

export default Style;