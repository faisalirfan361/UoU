import React, { useState } from "react";
import {
  LANGUAGE_OPTIONS,
  TIMEZONE_OPTIONS,
  RECURRENCE_OPTIONS,
  DEFAULT_ROLES,
  SUCCESS_TOAST_OPTIONS,
  ERROR_TOAST_OPTIONS,
} from "../../../../constants";
import {
  Grid,
  TextField,
  Typography,
  Select,
  MenuItem,
  InputLabel,
} from "@material-ui/core";
import usePointsCoinStoreStyle from "./styles";
import { ButtonActionDuel } from "components";
import Payment from "./Payment";

const PointsCoinStore = () => {
  const classes = usePointsCoinStoreStyle();
  const Divider = () => <div className={classes.divider} />;
  const [currency, setCurrency] = useState("USD");
  const [name, setName] = useState("");
  const [ownerName, setOwnerName] = useState("");
  const [ownerEmail, setOwnerEmail] = useState("");
  const [amount, setAmount] = useState("");

  const handleCurrencyChange = (event: React.ChangeEvent<{ value: any }>) => {
    setCurrency(event.target.value as string);
  };

  const saveHandler = () => {};
  return (
    <>
      <Divider />
      <Grid container>
        <Grid item xs={12}>
          <Typography component="span" className={classes.formHeading}>
            Coin Store
          </Typography>
        </Grid>
        <Grid
          container
          xs={11}
          spacing={5}
          className={classes.budgetInputContainer}
        >
          <Grid item xs={6}>
            <TextField
              id="point-coin-store-name"
              label="Account Name"
              variant="outlined"
              fullWidth={true}
              value={name}
              onChange={(event) => {
                setName(event.target.value);
              }}
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              id="point-coin-store-owner-name"
              label="Owner Name"
              variant="outlined"
              fullWidth={true}
              value={ownerName}
              onChange={(event) => {
                setOwnerName(event.target.value);
              }}
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
              id="point-coin-store-email"
              label="Owner Email"
              variant="outlined"
              fullWidth={true}
              value={ownerEmail}
              onChange={(event) => {
                setOwnerEmail(event.target.value);
              }}
            />
          </Grid>

          <Grid item xs={4}>
            <TextField
              id="point-coin-store-amount"
              label="Fund Amount"
              variant="outlined"
              fullWidth={true}
              type="number"
              className={classes.pointsInput}
              value={amount}
              onChange={(event) => {
                setAmount(event.target.value);
              }}
            />
          </Grid>
          <Grid item xs={2}>
            <Select
              labelId="demo-simple-select-outlined-label"
              id="point-coin-store-currency"
              variant="outlined"
              fullWidth={true}
              value={currency}
              onChange={handleCurrencyChange}
              label="Currency"
            >
              <MenuItem value={"USD"}>US Dollar</MenuItem>
              <MenuItem value={"GBP"}>GB Pound</MenuItem>
              <MenuItem value={"30"}>Euro</MenuItem>
            </Select>
          </Grid>
        </Grid>
      </Grid>
      <Grid container className={classes.paymentContainer}>
        <Grid item xs={12}>
          <label htmlFor="points" className={classes.formHeading}>
            Payment
          </label>
          <Payment />
        </Grid>
      </Grid>
      <Grid container className={classes.paymentContainer}>
        <ButtonActionDuel handleOnClick={saveHandler}>Save</ButtonActionDuel>
      </Grid>
    </>
  );
};

export default PointsCoinStore;
