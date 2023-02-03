import React, { ReactElement, useEffect, useRef, useState } from "react";
import Grid from "@material-ui/core/Grid";
import { ButtonActionDuel } from "components";
import useDuelContainerStyle from "./styles";
import CreateDuelModal from "../../../components/CreateDuelModal";
import { Can } from "context/Ability/Can";
import { useDepartments } from "../../../hooks/useDepartments";
import DepartmentSelector from "components/DepartmentSelector";
import { DuelCard } from "components";
import DuelProps from "components/DuelCard/types";
import { DuelType } from "components/GameCard/type";
import DuelCardActive from "components/DuelCardActive";
import GameFilter from "components/GameFilter";
import { Filter } from "components/GameFilter/types";
import { API } from "aws-amplify";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import config from "../../../config";
import { uniqBy } from "lodash";
import CircularProgress from "@material-ui/core/CircularProgress";
import InfiniteScroll from "react-infinite-scroll-component";

const Duel = () => {
  const classes = useDuelContainerStyle();
  const {
    departments,
    selectedDepartment,
    setSelectedDepartment,
    defaultDepartment,
  } = useDepartments();
  const { clientId, userId } = useRecoilValue(userAtom);
  const [showDuelModal, setShowDuelModal] = React.useState(false);
  // const [activeDuels, setActiveDuels] = useState<DuelType[]>([]);
  // const [newDuels, setNewDuels] = useState<DuelType[]>([]);
  // const [oldDuels, setOldDuels] = useState<DuelType[]>([]);
  const [duels, setDuels] = useState<DuelType[]>([]);
  const [unfiltersDuels, setUnfiltersDuels] = useState<DuelType[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [filter, setFilter] = useState<string>("active");
  const [hasMore, setHasMore] = useState(true);
  const myRef = useRef(null);
  const handleShowCreateNewDuel = () => {
    setShowDuelModal(true);
  };
  const handleCloseCreateNewDuel = () => {
    getDuels();
    setShowDuelModal(false);
  };

  const getDuels = async (lastKey?: string) => {
    let path = "/game/get-by-search";
    const body = {
      term: clientId,
      column: "CLIENT_ID",
      type: "DUELS",
      lastKey: lastKey,
      filter: filter,
    };
    try {
      const data = await API.post(config.apiGateway.NAME, path, {
        body: body,
      });
      if (data) {
        const bodyData = JSON.parse(data.body);
        const duelsData = Array.isArray(bodyData)
          ? bodyData.filter((item: any) => filterDuels(item))
          : [];
        if (bodyData.length < 10) {
          setLoading(false);
          setHasMore(false);
        }
        setDuels((prev) => uniqBy(prev.concat(duelsData), "gameId"));
        setUnfiltersDuels((prev) =>
          uniqBy(prev.concat(Array.isArray(bodyData) ? bodyData : []), "gameId")
        );
      }
    } catch (error) {
      console.log('Error "getDuels()"', error);
    }
  };

  const filterDuels = (duel: any) => {
    const startDate = Date.parse(duel.start_date);
    const now = Date.parse(new Date().toLocaleString());
    if (filter === "active") return getDuelStatus(duel) <= 1;
    return (
      duel && duel.profiles.length > 1 && (duel.isAccepted || startDate > now)
    );
  };

  const getDuelStatus = (duel: DuelType) => {
    const startDate = Date.parse(duel.start_date);
    const endDate = Date.parse(duel.end_date);
    const now = Date.parse(new Date().toISOString());

    const isComplete = duel.isComplete;
    const winnerProfile = duel.winnerProfile;

    if (startDate > now && !duel.isAccepted) {
      return 0; // return 1 if duel is New
    }
    if (isComplete && winnerProfile) {
      return 3; // return 3 if challenge have a winner
    } else if (isComplete) {
      return 2; // return 2 if challenge is Draw
    } else {
      return endDate > now ? 1 : 2; // return 1 if challenge is Active
    }
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([getDuels()]).then(() => {
      setLoading(false);
    });
  }, []);
  useEffect(() => {
    setLoading(true);
    setHasMore(true);
    setDuels([]);
    setUnfiltersDuels([]);
    Promise.all([getDuels()])
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, [filter]);

  // useEffect(() => {
  //   handleClassifyDuels();
  // }, [duels]);
  // const handleClassifyDuels = () => {
  //   const currentDate = new Date();
  //   const active = duels.filter(
  //     (item: any) => currentDate < new Date(item.end_date) && item.isAccepted
  //   );
  //   const newToAccept = duels.filter(
  //     (item: any) =>
  //       currentDate < new Date(item.start_date) &&
  //       !item.isAccepted &&
  //       !item.isDeclined
  //   );
  //   const expired = duels.filter(
  //     (item: any) => currentDate > new Date(item.end_date) && item.isAccepted
  //   );

  //   setActiveDuels(_.sortBy(active, "end_date").reverse());
  //   setNewDuels(_.sortBy(newToAccept, "end_date").reverse());
  //   setOldDuels(_.sortBy(expired, "end_date").reverse());
  // };

  const lastDuel = unfiltersDuels[unfiltersDuels.length - 1];
  const lastKey = lastDuel && lastDuel.gameId;
  const loadMore = async () => {
    setLoading(true);
    await Promise.all([getDuels(lastKey)]).then(() => {
      setLoading(false);
    });
  };

  const Loading = (
    <div className={classes.loading}>
      <CircularProgress className={classes.CircularProgress} />
    </div>
  );
  const handleShowCard = (duel: any, index: number) => {
    const currentDate = new Date();
    const amItheparticipant = duel.profiles.find(
      (profile: any) => profile.entityId === userId
    );
    if (currentDate < new Date(duel.end_date) && duel.isAccepted) {
      return amItheparticipant ? (
        <Grid item xs={12} key={`active-duel-card-${index}`}>
          <DuelCardActive duel={duel} />
        </Grid>
      ) : (
        <Grid item xs={12} sm={6} md={6} lg={6} key={`old-duel-card-${index}`}>
          <DuelCard duel={duel} filter={filter} refresh={() => getDuels()} />
        </Grid>
      );
    }

    return (
      <Grid item xs={12} sm={6} md={6} lg={6} key={`new-duel-card-${index}`}>
        <DuelCard duel={duel} filter={filter} refresh={() => getDuels()} />
      </Grid>
    );
  };
  const setSelectedFilter = (opt: Filter) => {
    if (opt?.name) {
      setFilter(opt.value);
    }
  };

  return (
    <>
      <Grid container className={classes.duelContainer}>
        <GameFilter onSelect={setSelectedFilter} />
        <Grid item xs={12}>
          <Can I="create" a="game-duels">
            <span className={classes.createDuelButtonContainer}>
              <ButtonActionDuel handleOnClick={handleShowCreateNewDuel}>
                Create New Duel
              </ButtonActionDuel>
            </span>
          </Can>
        </Grid>
      </Grid>
      <InfiniteScroll
        next={loadMore}
        hasMore={hasMore}
        dataLength={duels.length}
        loader={Loading}
        scrollableTarget={myRef}
        className={classes.infiniteScroll}
      >
        <Grid container spacing={4} className={classes.DuelCardContainer}>
          {duels ? (
            <>{React.Children.toArray(duels.map(handleShowCard))}</>
          ) : (
            <Grid item xs={12} className={classes.duelTextContainer}>
              <div className={classes.duelText}>
                You are not currently in any duels.{" "}
                <span className={classes.duelTextSpan}>Create New Duel</span> to
                get started.
              </div>
            </Grid>
          )}
        </Grid>
        {loading && !lastKey && Loading}
      </InfiniteScroll>
      <CreateDuelModal
        onClose={handleCloseCreateNewDuel}
        open={showDuelModal}
      />
    </>
  );
};

export default Duel;
