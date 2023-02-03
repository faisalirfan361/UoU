import { memo, useState } from "react";
import PetsIcon from "@material-ui/icons/Pets";
import { Typography, Grid } from "@material-ui/core";
import { useRecoilState } from "recoil";
import { API } from "aws-amplify";
import useSWR from "swr";
import { useSnackbar } from "notistack";
import { userAtom } from "state";

import {
  ERROR_TOAST_OPTIONS,
  MenuVariant,
  SUCCESS_TOAST_OPTIONS,
} from "../../../../constants";

import PaginationComponent from "components/Pagination";

import CoinStoreItem from "components/CoinStoreItem";
import useStyle from "./style";
import config from "config";

import MainHeader from "components/MainHeader";
import { formatDescription } from "utils/formatters";

const PAGE_SIZE = 20;

const providersApiPayload = {
  path: "",
  method: "GET",
};

const tangoItemsApiPayload = {
  path: "",
  method: "GET",
};

const getProviderId = (providersData: any) => {
  if (Array.isArray(providersData) && providersData.length > 0) {
    return providersData.find((p) => p._name === "Tango Cards")._id || "1";
  }
  return "1";
};

const CoinStoreContainer = () => {
  const styles = useStyle();
  const { enqueueSnackbar } = useSnackbar();

  const [userAtomState, setUserAtomState] = useRecoilState(userAtom);

  const {
    clientId,
    departmentId,
    email,
    userId,
    pointsBalance,
    pointsCumulative,
    avatarImages,
  } = userAtomState;

  const [pageIndex, setPageIndex] = useState(1);

  const avatarSrc = `${config.targetBucketUrl}${avatarImages?.keys.large}`;

  const performanceHeaderProps = {
    imageUrl: avatarSrc,
    level: {
      icon: PetsIcon,
      name: "Puppy",
      levelNumber: 1,
    },
    pointsToLevelUp: 0,
    challengesWon: 0,
    points: pointsCumulative,
    coins: pointsBalance,
  };

  providersApiPayload.path = `/raas/clients/${clientId}/raas-providers`;

  const { data: providersData } = useSWR(
    [providersApiPayload.path, providersApiPayload],
    { suspense: false }
  );

  const providerId = getProviderId(providersData);

  tangoItemsApiPayload.path = `/raas/clients/${clientId}/raas-providers/${providerId}/items`;

  const {
    data: itemsData = [],
    isValidating,
    mutate,
  } = useSWR(
    [
      `${tangoItemsApiPayload.path}?size=${PAGE_SIZE}&page=${pageIndex}`,
      tangoItemsApiPayload,
    ],
    {
      suspense: false,
    }
  );

  const handleClick = (item: any) => {
    API.post("ApiGateway", "/raas/points/redemption", {
      body: {
        userId,
        itemId: item._id,
        receiverEmail: email,
      },
    })
      .then((transactionSummary) => {
        console.log("transactionSummary ", transactionSummary);

        setUserAtomState({
          ...userAtomState,
          pointsBalance:
            userAtomState.pointsBalance - transactionSummary._points,
        });

        mutate();

        enqueueSnackbar("Points redeemed successfully", SUCCESS_TOAST_OPTIONS);
      })
      .catch(() => {
        enqueueSnackbar("Error trying to redeem points", ERROR_TOAST_OPTIONS);
      });
  };

  const onPaginationChanged = (
    event: React.ChangeEvent<unknown>,
    value: number
  ) => {
    setPageIndex(value);
  };

  const coinStoreItems =
    itemsData && itemsData._items
      ? itemsData._items
          .map((item: any) => {
            return {
              imageUrl: item?._images[0]?._url,
              title: item._title,
              description: formatDescription(item._description),
              points: item._points,
              canRedeem: item._canRedeem,
              onClick: () => handleClick(item),
            };
          })
          .filter((_: any, index: number) => {
            return index <= 50;
          })
      : [];

  return (
    <>
      <MainHeader>
        <MainHeader.PerformanceBar showAvatar={true} />
      </MainHeader>

      <Grid container direction="row" className={styles.row}>
        {isValidating && <Typography>Loading...</Typography>}
      </Grid>
      <Grid container direction="row" className={styles.row}>
        {coinStoreItems.map((props: any, index: number) => (
          <Grid item xs={3} className={styles.item} key={index}>
            <CoinStoreItem {...props} />
          </Grid>
        ))}
      </Grid>
      <Grid container direction="row" className={styles.paginationContainer}>
        <PaginationComponent
          disabled={isValidating}
          totalPages={itemsData._totalPages}
          onChanged={onPaginationChanged}
        />
      </Grid>
    </>
  );
};

export default memo(CoinStoreContainer);
