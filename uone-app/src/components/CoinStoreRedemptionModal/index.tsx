import React from "react";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import CardMedia from "@material-ui/core/CardMedia";
import Typography from "@material-ui/core/Typography";
import CoinStoreRedemptionModalProps from "./types";
import CheckIcon from "@material-ui/icons/Check";
import {
  Box,
  Button,
  Dialog,
  DialogContentText,
  Link,
  useTheme,
} from "@material-ui/core";

import useStyle from "./style";
import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";

const HtmlToReactParser = require("html-to-react").Parser;
const htmlToReactParser = new HtmlToReactParser();

const CoinStoreRedemptionModal: React.FC<CoinStoreRedemptionModalProps> = ({
  selectedItem,
  open,
  onClose,
}) => {
  const theme = useTheme();
  const classes = useStyle();
  const { username: email } = useRecoilValue(userAtom);

  return (
    <div>
      <Dialog open={open} onClose={onClose}>
        <UOneDialogTitle id="coin-store-redeem-success" onClose={onClose}>
          <Box component="span" display="flex" alignItems="flex-end">
            <CheckIcon color="primary" />
            Success!
          </Box>
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <DialogContentText>
            <Typography>
              Check your inbox at <Link href={`mailto:${email}`}>{email}</Link>{" "}
              for your reward selection.
            </Typography>
          </DialogContentText>

          <Card
            className={classes.itemCard}
            style={{ backgroundColor: theme.common.grey[200] }}
          >
            <CardMedia
              className={classes.mainImage}
              image={selectedItem?.imageUrl}
            />

            <div className={classes.details}>
              <CardContent className={classes.content}>
                <Typography component="h5" variant="h5">
                  {selectedItem?.name}
                </Typography>
              </CardContent>
            </div>
          </Card>

          <DialogContentText className={classes.description}>
            {htmlToReactParser.parse(selectedItem?.terms)}
          </DialogContentText>
        </UOneDialogContent>
        <UOneDialogActions>
          <Button onClick={onClose}>Done</Button>
        </UOneDialogActions>
      </Dialog>
    </div>
  );
};

export default CoinStoreRedemptionModal;
