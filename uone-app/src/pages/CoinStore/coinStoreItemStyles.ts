import { makeStyles } from "@material-ui/core/styles";

const coinStoreItemClasses = makeStyles((theme) => ({
  coinStoreItem: {
    padding: "1em",
    minWidth: "200px",
    height: "100%",
    minHeight: "250px",
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
  },
  coinStoreItemThumbnail: {
    width: "80px",
    height: "50px",
    borderRadius: "4px",
    border: `1px solid ${theme.common.uoneLightBlue[500]}`,
  },
  coinStoreItemContent: {
    flex: 1,
    flexDirection: "column",
    display: "flex",
    padding: `${theme.spacing(2)}px 0`,
    width: "100%",
  },
  coinStoreItemAction: {
    display: "flex",
    justifyContent: "space-between",
    width: "100%",
  },
  coinStoreButton: {
    float: "right",
    width: 30,
    height: 30,
  },

  cardHeader: {
    padding: 0,
    width: "100%",
  }
}));

export default coinStoreItemClasses;
