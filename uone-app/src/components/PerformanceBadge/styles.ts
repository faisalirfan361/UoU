import { makeStyles } from "@material-ui/core";

export default makeStyles((theme) => ({
  performanceBadge: {
    width: "50px",
    height: "50px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    borderRadius: "50%",
    overflow: "hidden",
    boxShadow: theme.shadows[3],
  },
  performanceBadgeInner: {
    width: "40px",
    height: "40px",
    display: "flex",
    borderRadius: "50%",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "space-around",
  },
  performanceBadgeIcon: {
    lineHeight: "1em",
    fontSize: "1.4em",
    height: "24px",
    display: "flex",
    alignItems: "flex-end",
    justifyContent: "end",
    marginBottom: "2px",
    marginTop: "2px",
    color: theme.palette.common.white,
    filter: "drop-shadow(0 0 4px rgba(0,0,0,0.5))",
  },
  performanceBadgeLabel: {
    background: theme.palette.common.black,
    color: theme.palette.common.white,
    fontSize: "7px",
    textTransform: "uppercase",
    fontWeight: "bold",
    width: "50px",
    textAlign: "center",
    padding: "2px",
    "-webkit-font-smoothing": "auto",
    pointerEvents: "none",
  },
  bronze: {
    background:
      "linear-gradient(216.59deg, rgb(161,121,66) 15.79%, rgb(218,204,152) 85.32%)",
  },
  bronzeInner: {
    background: "linear-gradient(216.59deg, #8D5807 15.79%, #D7C589 85.32%)",
  },
  silver: {
    background:
      "linear-gradient(216.59deg, rgb(167,167,167) 15.79%, rgb(199,199,199) 85.32%)",
  },
  silverInner: {
    background: "linear-gradient(216.59deg, #939393 15.79%, #C1C1C1 85.32%)",
  },
  gold: {
    background:
      "linear-gradient(216.59deg, rgb(232,179,79) 15.79%, rgb(249,212,105) 85.32%)",
  },
  goldInner: {
    background: "linear-gradient(216.59deg, #E4A01B 15.79%, #FCD248 85.32%)",
  },
  platinum: {
    background:
      "linear-gradient(216.59deg, rgb(231,231,231) 15.79%, rgb(239,239,239) 85.32%)",
  },
  platinumInner: {
    background: "linear-gradient(216.59deg, #E3E3E3 15.79%, #EEEEEE 85.32%)",
  },
  diamond: {
    background:
      "linear-gradient(216.59deg, rgb(136,216,253) 15.79%, rgb(200,230,245) 85.32%)",
  },
  diamondInner: {
    background: "linear-gradient(216.59deg, #5FCFFF 15.79%, #CAE5F1 85.32%)",
  },
  black: {
    background:
      "linear-gradient(216.59deg, rgb(67,67,67) 15.79%, rgb(131,131,131) 85.32%)",
  },
  blackInner: {
    background: "linear-gradient(216.59deg, #000000 15.79%, #757575 85.32%)",
  },
  pink: {
    background: "linear-gradient(315deg, #e899dc 0%, #d387ab 74%)",
  },
  pinkInner: {
    background: "linear-gradient(315deg, #d387ab 0%, #e899dc 74%)",
  },
}));

export const BadgeSkeletonStyles = makeStyles((theme) => ({
  root: {
    border: `2px solid ${theme.common.grey[200]}`,
    backgroundColor: `${theme.palette.common.white} !important`,
  },
}));
