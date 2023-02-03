import React, { useEffect, useState } from "react";
import SelectIndicatorModalProps from "./types";
import { useRecoilValue } from "recoil";
import _get from "lodash.get";
import {
  Button,
  Dialog,
  Grid,
  Typography,
  InputBase,
  Checkbox,
  Box,
  FormControlLabel,
  RadioGroup,
  Radio,
} from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { userAtom } from "state";

import { ButtonActionDuel, ButtonOutlinedComponent } from "components";
import RHFRadioButtonComponent from "components/RHFRadioButton";
import CreateGoal from "components/GoalForm/GoalCreate";
import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";
import { useIndicator, Indicator } from "hooks/useIndicators";
import useSelectIndicatorModalStyle from "./style";
import CreateIndicatorModal from "components/CreateIndicatorModal";

const SelectIndicatorModal: React.FC<SelectIndicatorModalProps> = ({
  open,
  onClose,
  onRefresh,
  departmentId,
}) => {
  const classes = useSelectIndicatorModalStyle();
  const { clientId } = useRecoilValue(userAtom);
  const [searchTerm, setSearchTerm] = useState("");
  const [createIndicatorModalOpen, setCreateIndicatorModalOpen] =
    useState(false);

  const { indicatorsData, forceRequest } = useIndicator(clientId);
  const [indicatorsOptions, setIndicatorsOptions] = useState<Indicator[]>([]);
  const [originalIndicatorsOptions, setOriginalIndicatorsOptions] = useState<
    Indicator[]
  >([]);
  const [indicatorId, setIndicatorId] = useState<string>("");
  const [createGoalModalOpen, setCreateGoalModalOpen] = useState(false);

  useEffect(() => {
    if (indicatorsData && indicatorsData.length > 0) {
      setIndicatorsOptions([...indicatorsData]);
      setOriginalIndicatorsOptions([...indicatorsData]);
    }
  }, [indicatorsData]);

  const indicatorRadioOnChange = (event: any) => {
    setIndicatorId(event.target.value);
  };

  const getSelectedIndicator = () => {
    return indicatorsOptions.find((i) => i.entityId == indicatorId);
  };

  //useEffect for searching indicators
  useEffect(() => {
    if (!searchTerm || searchTerm.length == 0) {
      setIndicatorsOptions(originalIndicatorsOptions);
    } else {
      const searchResult: Indicator[] = originalIndicatorsOptions.filter(
        (indicator: Indicator) =>
          indicator.attributes.name
            .toLowerCase()
            .includes(searchTerm.toLowerCase())
      );
      setIndicatorsOptions(searchResult);
    }
  }, [searchTerm]);

  //Create indicator modal
  const closeCreateModalIndicator = () => {
    setCreateIndicatorModalOpen(false);
  };
  const openCreateModalIndicator = () => {
    setIndicatorId("");
    setCreateIndicatorModalOpen(true);
  };

  //Create Goal Modal
  const closeCreateGoalModal = () => {
    setCreateGoalModalOpen(false);
  };
  const openCreateGoalModal = () => {
    if (indicatorId.length > 0) {
      setCreateGoalModalOpen(true);
    }
  };

  const createGoalModalCallback = () => {
    closeCreateGoalModal();
    onRefresh();
    onClose();
  };

  const refreshIndicators = () => {
    forceRequest();
  };

  return (
    <>
      <form>
        <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"md"}>
          <UOneDialogTitle id="select-indicator-modal" onClose={onClose}>
            Select Indicators
          </UOneDialogTitle>
          <UOneDialogContent dividers>
            <Box className={classes.popupContainer}>
              <Grid container>
                <Grid item xs={12} className={classes.searchWrapper}>
                  <SearchIcon className={classes.searchIcon} />
                  <InputBase
                    className={classes.searchInput}
                    placeholder="Search Indicator..."
                    inputProps={{ "aria-label": "Search Indicator..." }}
                    onChange={(event) => {
                      setSearchTerm(event.target.value);
                    }}
                    value={searchTerm}
                  />
                </Grid>
                <Grid
                  container
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                  className={classes.newIndicatorContainer}
                >
                  <Typography component="span" className={classes.Typoselect}>
                    Select an indicator
                  </Typography>
                  <ButtonOutlinedComponent
                    handleOnClick={openCreateModalIndicator}
                  >
                    Create New Indicator
                  </ButtonOutlinedComponent>
                </Grid>
                <Grid container>
                  <Grid container>
                    <Grid item xs={2}>
                      <Typography
                        component="span"
                        className={classes.typoKpiListHeading}
                      >
                        SELECT
                      </Typography>
                    </Grid>
                    <Grid item xs={10}>
                      <Typography
                        component="span"
                        className={classes.typoKpiListHeading}
                      >
                        Indicator
                      </Typography>
                    </Grid>
                  </Grid>
                  <Grid container>
                    <Grid item xs={12}>
                      {indicatorsOptions ? (
                        <RadioGroup
                          aria-label="indicator-radio-button"
                          onChange={indicatorRadioOnChange}
                          name="indicator-radio-buttons"
                          value={indicatorId}
                        >
                          {indicatorsOptions.map(
                            (indicator: Indicator, index: number) => (
                              <FormControlLabel
                                key={`radioBtn-${indicator.entityId}`}
                                value={indicator.entityId}
                                control={<Radio />}
                                label={indicator.attributes.name}
                              />
                            )
                          )}
                        </RadioGroup>
                      ) : (
                        ""
                      )}
                    </Grid>
                  </Grid>
                </Grid>
              </Grid>
            </Box>
          </UOneDialogContent>
          <UOneDialogActions>
            <Button
              onClick={onClose}
              size="small"
              className={classes.btnCancel}
            >
              Cancel
            </Button>
            <ButtonActionDuel
              handleOnClick={openCreateGoalModal}
              disabled={indicatorId.length == 0 ? true : false}
            >
              Set Up Goal
            </ButtonActionDuel>
          </UOneDialogActions>
        </Dialog>
      </form>

      <CreateIndicatorModal
        onClose={closeCreateModalIndicator}
        open={createIndicatorModalOpen}
        departmentId={departmentId}
        callback={refreshIndicators}
      />

      <CreateGoal
        onClose={closeCreateGoalModal}
        open={createGoalModalOpen}
        departmentId={departmentId}
        indicator={getSelectedIndicator()}
        createGoalCallback={createGoalModalCallback}
      />
    </>
  );
};

export default SelectIndicatorModal;
