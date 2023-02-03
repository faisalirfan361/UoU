import React, { useState } from "react";
import { Grid, TextField } from "@material-ui/core";
import usePaymentStyle from "../styles";
import { usePaymentInputs } from "react-payment-inputs";

const Payment = () => {
  const classes = usePaymentStyle();
  const {
    getCardNumberProps,
    get,
    getZIPProps,
    getExpiryDateProps,
    getCVCProps,
  } = usePaymentInputs();
  const [cardNumber, setCardNumber] = useState("");
  const [cardName, setCardName] = useState("");
  const [cardExpire, setCardExpire] = useState("");
  const [cardZip, setCardZip] = useState("");
  const [cardCVV, setCardCVV] = useState("");
  const [cardAddress, setCardAddress] = useState("");
  const [cardState, setCardState] = useState("");
  return (
    <>
      <Grid
        container
        xs={11}
        spacing={5}
        className={classes.budgetInputContainer}
      >
        <Grid item xs={6}>
          <TextField
            id="payment-number"
            label="Credit Card Number"
            variant="outlined"
            fullWidth={true}
            value={cardNumber}
            onChange={(event) => setCardNumber(event.target.value)}
            inputProps={getCardNumberProps({})}
          />
        </Grid>
        <Grid item xs={6}>
          <TextField
            id="payment-name"
            label="Name on Credit Card"
            variant="outlined"
            fullWidth={true}
            value={cardName}
            onChange={(event) => setCardName(event.target.value)}
          />
        </Grid>
      </Grid>
      <Grid
        container
        xs={11}
        spacing={5}
        className={classes.budgetInputContainer}
      >
        <Grid item xs={6}>
          <TextField
            id="payment-exp"
            label="Address"
            variant="outlined"
            fullWidth={true}
            value={cardAddress}
            onChange={(event) => setCardAddress(event.target.value)}
          />
        </Grid>

        <Grid item xs={4}>
          <TextField
            id="payment-state"
            label="State"
            variant="outlined"
            fullWidth={true}
            value={cardState}
            onChange={(event) => setCardState(event.target.value)}
          />
        </Grid>
        <Grid item xs={2}>
          <TextField
            id="payment-zip"
            label="Zip Code"
            variant="outlined"
            fullWidth={true}
            inputProps={getZIPProps({})}
            value={cardZip}
            onChange={(event) => setCardZip(event.target.value)}
          />
        </Grid>
      </Grid>
      <Grid
        container
        xs={11}
        spacing={5}
        className={classes.budgetInputContainer}
      >
        <Grid item xs={4}>
          <TextField
            id="payment-exp"
            label="Expiration Date"
            variant="outlined"
            fullWidth={true}
            inputProps={getExpiryDateProps({})}
            value={cardExpire}
            onChange={(event) => setCardExpire(event.target.value)}
          />
        </Grid>

        <Grid item xs={2}>
          <TextField
            id="payment-cvv"
            label="CVV"
            variant="outlined"
            fullWidth={true}
            inputProps={getCVCProps({})}
            value={cardCVV}
            onChange={(event) => setCardCVV(event.target.value)}
          />
        </Grid>
      </Grid>
    </>
  );
};

export default Payment;
