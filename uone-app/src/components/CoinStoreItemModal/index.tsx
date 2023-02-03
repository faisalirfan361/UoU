import React from "react";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import CardMedia from "@material-ui/core/CardMedia";
import Typography from "@material-ui/core/Typography";
import FormGroup from "@material-ui/core/FormGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Checkbox from "@material-ui/core/Checkbox";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import CheckBoxIcon from "@material-ui/icons/CheckBox";
import CoinStoreItemModalProps from "./types";
import { Button, Dialog, DialogContentText, useTheme } from "@material-ui/core";
import { API } from "aws-amplify";
import { useRecoilState } from "recoil";
import { userAtom } from "state";
import { useSnackbar } from "notistack";

import coinStoreItemModalStyles from "./style";
import config from "../../config";
import {
  UOneDialogTitle,
  UOneDialogContent,
  UOneDialogActions,
} from "components/UOneDialog";
import { ERROR_TOAST_OPTIONS } from "../../constants";

const HtmlToReactParser = require("html-to-react").Parser;
const htmlToReactParser = new HtmlToReactParser();

const CoinStoreItemModal: React.FC<CoinStoreItemModalProps> = ({
  selectedItem,
  open,
  onRedeem,
  onClose,
}) => {
  const theme = useTheme();
  const [
    { userId, username: email, pointsBalance, clientId },
    setUserAtomState,
  ] = useRecoilState(userAtom);
  const { enqueueSnackbar } = useSnackbar();
  const classes = coinStoreItemModalStyles();
  const [acceptRedeem, setAcceptRedeem] = React.useState(false);
  const [btnLock, setBtnLock] = React.useState(false);

  const handleRedeemItem = async () => {
    setBtnLock(true);
    try {
      const result = await API.post(
        config.apiGateway.NAME,
        `/raas/points/redemption`,
        {
          body: {
            userId,
            clientId,
            itemId: selectedItem.id,
            receiverEmail: email,
          },
        }
      );
      setUserAtomState((state) => ({
        ...state,
        pointsBalance: pointsBalance - result._points,
      }));
      onRedeem();
    } catch (error) {
      console.log(error);
      enqueueSnackbar("Error trying to redeem points", ERROR_TOAST_OPTIONS);
    }
    setBtnLock(false);
  };

  return (
    <div>
      <Dialog open={open} onClose={onClose}>
        <UOneDialogTitle id="coin-store-item-modal" onClose={onClose}>
          {selectedItem?.brand}
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <Card
            className={classes.itemCard}
            style={{ backgroundColor: theme.common?.grey[200] }}
          >
            <CardMedia
              className={classes.mainImage}
              image={selectedItem?.imageUrl}
            />

            <div className={classes.details}>
              <CardContent className={classes.content}>
                <Typography component="h5" variant="h5">
                  {selectedItem?.title}
                </Typography>
                <Typography variant="subtitle1" color="primary">
                  {`${selectedItem?.points} coins`}
                </Typography>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={acceptRedeem}
                      onChange={() => setAcceptRedeem(!acceptRedeem)}
                      name="checkedB"
                      color="primary"
                    />
                  }
                  classes={{
                    labelPlacementStart: classes.labelPlacementStart,
                    label: classes.label,
                  }}
                  labelPlacement="start"
                  label="I understand that I am redeeming this item."
                />
              </CardContent>
            </div>
            <div className={classes.redeemContainer}>
              <Button
                variant="contained"
                color="primary"
                disabled={!acceptRedeem || btnLock}
                onClick={handleRedeemItem}
              >
                Redeem
              </Button>
            </div>
          </Card>

          <DialogContentText className={classes.description}>
            {htmlToReactParser.parse(selectedItem?.terms)}
          </DialogContentText>
        </UOneDialogContent>
        <UOneDialogActions>
          <Button
            onClick={onClose}
            variant="outlined"
            color="primary"
            size="small"
          >
            Back to Catalog
          </Button>
        </UOneDialogActions>
      </Dialog>
    </div>
  );
};

export default CoinStoreItemModal;
