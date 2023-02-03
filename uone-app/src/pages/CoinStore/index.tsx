import { useEffect, useReducer } from "react";
import { useRecoilValue } from "recoil";
import { Grid, Typography, Box, Button } from "@material-ui/core";
import Pagination from "@material-ui/lab/Pagination";
import useSWR from "swr";
import { useLayoutContext } from "layouts/LayoutProvider";
import { userAtom } from "state";

import useCoinStoreStyles from "./styles";
import MainHeader from "components/MainHeader";
import { Can } from "context/Ability/Can";
import CoinStoreItem from "./CoinStoreItem";
import { formatDescription } from "utils/formatters";
import CoinStoreItemSkeleton from "./CoinStoreItemSkeleton";
import CoinStoreItemModal from "components/CoinStoreItemModal";
import CreateCoinStoreItemModal from "components/CreateCoinStoreItemModal";
import CoinStoreRedemptionModal from "components/CoinStoreRedemptionModal";

const initialState = {
  page: 1,
  pageSize: 12,
  showItemModal: false,
  showCreateItemModal: false,
  editItem: false,
  showSuccessRedeemedModal: false,
  selectedItem: null,
};

const SET_PAGE = "SET_PAGE";
const SET_PAGE_SIZE = "SET_PAGE_SIZE";
const TOGGLE_CREATE_ITEM_MODAL = "TOGGLE_CREATE_ITEM_MODAL";
const TOGGLE_REDEEM_ITEM_MODAL = "TOGGLE_REDEEM_ITEM_MODAL";
const TOGGLE_REDEEM_SUCCESS_MODAL = "TOGGLE_REDEEM_SUCCESS_MODAL";
const SELECT_STORE_ITEM = "SELECT_STORE_ITEM";

const coinStoreReducer = (state: any, action: any) => {
  switch (action.type) {
    case SET_PAGE:
      return { ...state, page: action.page };
    case SET_PAGE_SIZE:
      return { ...state, pageSize: action.pageSize };
    case TOGGLE_CREATE_ITEM_MODAL:
      return {
        ...state,
        showCreateItemModal: action.status,
        editItem: action.editItem,
        selectedItem: action.selectedItem,
      };
    case TOGGLE_REDEEM_ITEM_MODAL:
      return { ...state, showItemModal: action.status };
    case TOGGLE_REDEEM_SUCCESS_MODAL:
      return { ...state, showSuccessRedeemedModal: action.status };
    case SELECT_STORE_ITEM:
      return { ...state, selectedItem: action.item };
  }
};

const CoinStore = () => {
  const classes = useCoinStoreStyles();
  const { setLayoutTitle } = useLayoutContext();
  const [state, dispatch] = useReducer(coinStoreReducer, initialState);
  const { clientId } = useRecoilValue(userAtom);
  const { data: itemsData, mutate: refetchStoreItems } = useSWR(
    `/raas/clients/${clientId}/raas-providers/items?size=${state.pageSize}&page=${state.page}`
  );
  const { data: pointsLimit, mutate: refetchPointsLimit } = useSWR(
    `/raas/clients/${clientId}/raas-providers/points-limit`
  );

  useEffect(() => {
    setLayoutTitle("Coin Store");
  }, [setLayoutTitle]);

  const handlePageChange = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    dispatch({ type: SET_PAGE, page: value });
  };

  const handleCloseCoinStoreItemModal = () => {
    dispatch({ type: TOGGLE_REDEEM_ITEM_MODAL, status: false });
    dispatch({ type: SELECT_STORE_ITEM, item: null });
  };

  const handleCloseSuccessRedeemedModal = () => {
    dispatch({ type: TOGGLE_REDEEM_SUCCESS_MODAL, status: false });
    dispatch({ type: SELECT_STORE_ITEM, item: null });
  };

  const handleCloseCreateItemModel = () => {
    refetchStoreItems();
    dispatch({
      type: TOGGLE_CREATE_ITEM_MODAL,
      status: false,
      editItem: false,
      selectedItem: null,
    });
  };

  const handleRedeemCallback = () => {
    refetchStoreItems();
    refetchPointsLimit();
    dispatch({ type: TOGGLE_REDEEM_ITEM_MODAL, status: false });
    dispatch({ type: TOGGLE_REDEEM_SUCCESS_MODAL, status: true });
  };

  const coinStoreItems =
    itemsData && itemsData._items
      ? itemsData._items.map((item: any) => {
          const detailImageUrl = item._images.find(
            (i: any) => i._status === "Active"
          );

          return {
            id: item._id,
            imageUrl: item?._images[0]?._url,
            title: item._title,
            description: formatDescription(item._description),
            points: item._points,
            canRedeem: item._canRedeem && !pointsLimit?.reachedDailyPoints,
            brand: item._brand,
            detailImageUrl: detailImageUrl ? detailImageUrl._url : "",
            terms: item._terms,
            instructions: item._redemptionInstructions,
            limit: item._maxValue,
            reachedDailyPoints: pointsLimit?.reachedDailyPoints,
          };
        })
      : new Array(10).fill(null);

  return (
    <>
      <Can I="view" a="my-performance">
        <MainHeader overflow="overlay">
          <MainHeader.PerformanceBar showAvatar={true} />
        </MainHeader>
      </Can>

      <Can I="create" a="coin-store">
        <Box mb={2} className={classes.adminActions}>
          <Button
            variant="contained"
            color="primary"
            onClick={() => {
              dispatch({ type: TOGGLE_CREATE_ITEM_MODAL, status: true });
            }}
          >
            Create Custom Card
          </Button>
        </Box>
      </Can>

      <Grid container spacing={4} wrap="wrap">
        {coinStoreItems.map((item: any, index: number) => (
          <Grid
            item
            lg={3}
            md={4}
            sm={6}
            xs={12}
            key={item?.id || `index-${index}`}
          >
            {item ? (
              <CoinStoreItem
                item={item}
                viewItem={() => {
                  dispatch({ type: SELECT_STORE_ITEM, item });
                  dispatch({ type: TOGGLE_REDEEM_ITEM_MODAL, status: true });
                }}
                editItem={() => {
                  dispatch({
                    type: TOGGLE_CREATE_ITEM_MODAL,
                    status: true,
                    editItem: true,
                    selectedItem: item,
                  });
                }}
              />
            ) : (
              <CoinStoreItemSkeleton />
            )}
          </Grid>
        ))}
      </Grid>

      <Box mt={2} className={classes.paginationBox}>
        <Pagination
          count={itemsData?._totalPages || 0}
          page={state.page}
          onChange={handlePageChange}
        />
      </Box>

      <Box mt={2}>
        <Typography className={classes.disclaimer} align={"center"}>
          *The merchants represented are not sponsors of the rewards or
          otherwise affiliated with Heyday Now. The logos and other identifying
          marks are trademarks of and owned by each represented company and/or
          its affiliates. Please visit each company's website for additional
          terms and conditions.
        </Typography>
      </Box>

      <CoinStoreItemModal
        selectedItem={state.selectedItem}
        onClose={handleCloseCoinStoreItemModal}
        open={state.showItemModal}
        onRedeem={handleRedeemCallback}
      />

      <CreateCoinStoreItemModal
        onClose={handleCloseCreateItemModel}
        open={state.showCreateItemModal}
        selectedItem={state.editItem ? state.selectedItem : null}
      />

      <CoinStoreRedemptionModal
        open={state.showSuccessRedeemedModal}
        onClose={handleCloseSuccessRedeemedModal}
        selectedItem={state.selectedItem}
      />
    </>
  );
};

export default CoinStore;
