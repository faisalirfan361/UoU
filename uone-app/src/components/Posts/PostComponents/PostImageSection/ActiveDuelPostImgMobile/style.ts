import { makeStyles } from "@material-ui/core/styles";

const Style = (props: any) =>
  makeStyles((theme) => ({
    blocks: {
      display: "flex",
      height: "260px",
    },
    block: {
      height: 180,
      width: "100%",
      position: "relative"
    },
    blockRightWrapper: {
      height: 180,
      width: "100%",
      position: "relative",
      marginTop: "-14rem",
    },
    blockLeft: {
      backgroundColor: "#999",
      backgroundImage: `url(${props.agent1})`,
      clipPath: "polygon(0 0, 100% 0, 100% 2.8rem, 0% 7.8rem)",
      opacity: 0.6,
    },
    blockRightContainer: {
      clipPath: "polygon(0 8.2rem, 100% 3.3rem, 100% 100%, 0% 100%)",
      backgroundImage: `url(${props.agent2})`,
      position: "absolute",
      top: "5px",
      left: "5px",
      right: "5px",
      bottom: "5px",
    },
    blockRight: {
      backgroundColor: "#5AD787",
      clipPath: "polygon(0 8.2rem, 100% 3.3rem, 100% 100%, 0% 100%)",
    },
    vsContainer: {
      position: "relative",
      minHeight: 202,
      width: "100%"
    },
    vsContainerA: {
      width: "100%",
      height: "260px",
      position: "absolute",
      top: 0,
      left: 0,
    },
    vsContainerB: {
      zIndex: 100,
      position: "inherit",
    },
    vsLeft: {
      width: "100%",
      display: "flex",
      justifyContent: "flex-start",
      marginLeft: theme.spacing(3),
      marginTop: -theme.spacing(7),
    },
    vsCenter: {
      minHeight: 202,
    },
    vsRight: {
      width: "100%",
      display: "flex",
      justifyContent: "flex-end",
      marginRight: theme.spacing(1),
      marginTop: theme.spacing(9.5),
    },
    vsCenterVSTextWrapper: {
      padding: theme.spacing(1),
      background: "#FFFFFF",
      borderRadius: "50%",
    },
    vsCenterVSText: {
      background: "#FFFFFF",
      width: 37,
      height: 37,
      lineHeight: 2,
      borderRadius: "50%",
      fontSize: 20,
      color: theme.common.uoneLightBlue[600],
      weight: 600,
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
    leftImageWrapper: {
      padding: "30px 0px"
    },
    rightImageWrapper: {
      padding: "30px 0px"
    },
    leftImage: {
      
    },
    rightImage: {
      borderColor:"#5AD787",
    },
    vsLeftContainer: {
      justifyContent: "center",
    },
    vsLeftDetailsBox: {
      display: "flex",
      flexDirection: "row",
      alignItems: "flex-start",
      padding: "2px 5px 2px 2px",
      background: "#FFFFFF",
      boxShadow: "0px 0px 3px rgba(0, 0, 0, 0.25)",
      borderRadius: "90px",
      height: "38px",
      marginLeft: theme.spacing(3)
    },
    vsLeftPosition: {
      width: 34,
      height: 34,
      borderRadius: "50px",
      background: "#CDCDCD",
    },
    vsLeftPositionText: {
      fontFamily: "Poppins",
      alignItems: "center",
      textAlign: "center",
      fontWeight: 800,
      fontSize: 20,
      color: "#464646",
      padding: "0px 10px",
    },
    vsLeftNameText: {
      alignItems: "center",
      textAlign: "center",
      margin: theme.spacing(0, 1.25),
      padding: theme.spacing(1,0),
      fontFamily: "Poppins",
      fontWeight: 800,
      fontSize: 13,
      color: "#464646",
    },
    vsLeftPointText: {
      alignItems: "center",
      textAlign: "center",
      padding: theme.spacing(1,0),
      fontFamily: "Poppins",
      fontWeight: 300,
      fontSize: 13,
      color: "#464646",
    },
    vsRightDetailsBox: {
      display: "flex",
      flexDirection: "row",
      alignItems: "flex-start",
      padding: "2px 5px 2px 2px",
      background: "#FFFFFF",
      boxShadow: "0px 0px 3px rgba(0, 0, 0, 0.25)",
      borderRadius: "90px",
      height: "38px",
      width: "max-content"
    },
    vsRightPosition: {
      width: 34,
      height: 34,
      borderRadius: "50px",
      background: "#5AD787",
    },
    vsRightPositionText: {
      fontFamily: "Poppins",
      alignItems: "center",
      textAlign: "center",
      fontWeight: 800,
      fontSize: 20,
      color: theme.palette.common.white,
      padding: "0px 12px"
    },
    vsRightNameText: {
      alignItems: "center",
      textAlign: "center",
      margin: theme.spacing(0, 1.25),
      padding: theme.spacing(1,0),
      fontFamily: "Poppins",
      fontWeight: 800,
      fontSize: 13,
      color: "#5AD787",
    },
    vsRightPointText: {
      alignItems: "center",
      textAlign: "center",
      padding: theme.spacing(1,0),
      fontFamily: "Poppins",
      fontWeight: 300,
      fontSize: 13,
      color: "#5AD787",
    },
  }));

export default Style;