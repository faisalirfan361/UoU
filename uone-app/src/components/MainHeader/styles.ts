import { makeStyles } from "@material-ui/core";

export const useBadgeAvatarStyles = makeStyles((theme) => ({
  root: {
    position: "absolute",
    bottom: (props: { position?: string }) =>
      props.position === "left" ? theme.spacing(-3) : theme.spacing(-6),
    left: (props: { position?: string }) =>
      props.position === "left"
        ? -theme.spacing(2)
        : `calc(50% - ${theme.spacing(20) / 2}px)`,
    border: `${theme.spacing(0.5)}px solid ${theme.palette.common.black}`,
    borderRadius: "50%",
    display: "block",
    "&::before": {
      display: "block",
      content: '" "',
      position: "absolute",
      zIndex: 1,
      borderRadius: "50%",
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      border: `${theme.spacing(0.8)}px solid ${theme.palette.common.white}`,
    },
  },
  badge: {
    top: "95%",
    right: "50%",
  },
}));

export const useMainHeaderStyles = makeStyles((theme) => ({
  largeAvatar: {
    height: theme.spacing(17),
    width: theme.spacing(17),
    fontSize: "3rem",
    background: theme.common.grey[900],
  },
  heroImage: {
    borderRadius: theme.spacing(0.5),
    background: theme.palette.background.paper,
    height: 276,
    backgroundSize: "cover",
    backgroundRepeat: "no-repeat",
    backgroundPosition: "center",
    boxShadow: '0px 1px 10px rgb(0 0 0 / 15%)',
  },
  bannerEditButton: {
    position: "absolute",
    top: "0.5rem",
    right: "0.5rem",
  },
  performanceList: {
    display: "flex",
    flexDirection: "row",
    padding: 0,
  },
  performanceListItem: {
    padding: 0,
    margin: "0 2em",
    color: theme.palette.common.white,
  },
  performanceListItemIcon: {
    fontSize: "2em",
    color: "inherit",
  },
  teamLabel: {
    fontWeight: "bold",
  },
}));

export const useMainHeaderCardStyles = makeStyles((theme) => ({
  root: {
    boxShadow: "none",
    background: "transparent",
    position: "relative",
    marginBottom: theme.spacing(5),
    overflow: (props: any) => props.overflow,
    width: "100%",
  },
}));

export const useMainHeaderCardContentStyles = makeStyles((theme) => ({
  content: {
    background: "#364556",
    marginTop: -theme.spacing(0.5),
    color: theme.palette.common.white,
    borderBottomLeftRadius: theme.spacing(1),
    borderBottomRightRadius: theme.spacing(0.5),
    paddingLeft: (props: any) =>
      props.showAvatar ? theme.spacing(4) : theme.spacing(20),
    paddingBottom: `${theme.spacing(2)}px !important`,
  },
}));

export const usePerformanceListItemTextStyles = makeStyles((theme) => ({
  primary: {
    fontWeight: "bold",
  },
  secondary: {
    color: theme.palette.common.white,
  },
}));
