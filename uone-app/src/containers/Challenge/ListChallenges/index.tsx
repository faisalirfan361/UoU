import React, { memo, useEffect, useRef, useState } from "react";
import Grid from "@material-ui/core/Grid";
import { API } from "aws-amplify";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import _get from "lodash.get";
import { Button, Typography } from "@material-ui/core";
import { Dialog } from "@material-ui/core";
import { useSnackbar } from "notistack";
import { Can } from "context/Ability/Can";
import InfiniteScroll from "react-infinite-scroll-component";
import CircularProgress from "@material-ui/core/CircularProgress";

import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import { UOneDialogTitle, UOneDialogContent } from "components/UOneDialog";
import CreateChallengeForm from "../Forms/CreateChallenge";
import EditChallengeForm from "../Forms/EditChallenge";
import AcceptCancelModal from "components/AcceptCancelModal";
import config from "../../../config";
import useListChallengesStyles from "./style";
import ChallengeSkeletonCard from "components/Challenge/ChallengeCard/ChallengeSkeletonCard";
import GameCard from "components/GameCard";

import { ColumnDefinition } from "components/GameCard/GameCardTable/GameCardTable";
import { ChallengeType } from "components/GameCard/type";
import { formatUsersRows } from "../utils";
import GameCardAgentCell from "components/GameCard/GameCardTable/GameCardAgentCell";
import GameFilter from "components/GameFilter";
import { Filter } from "components/GameFilter/types";
import {
  humanReadableDecimal,
  humanReadableTimeStamp,
} from "utils/humanReadable";
import { sortBy, uniqBy } from "lodash";
import { useIndicator, Indicator } from "hooks/useIndicators";

const ListChallenges = () => {
  const classes = useListChallengesStyles();
  const { enqueueSnackbar } = useSnackbar();
  const users = useRecoilValue(userAtom);
  const { clientId } = users;
  const [challenges, setChallenges] = useState<ChallengeType[]>([]);
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isEditModalOpen, setEditModalOpen] = useState(false);
  const [lockButton, setLockButton] = useState(false);
  const [currentChallenge, setCurrentChallenge] =
    useState<null | ChallengeType>();
  const [loading, setLoading] = useState(true);
  const myRef = useRef(null);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const { indicatorsData } = useIndicator(clientId);
  const [filter, setFilter] = useState<string>("active");
  const [hasMore, setHasMore] = useState<boolean>(true);

  const columns: ColumnDefinition[] = [
    { id: "rank", label: "Rank", className: classes.tableText },
    {
      id: "fullName",
      label: "Agent Name",
      className: [classes.tableText, classes.agentName].join(" "),
      component: GameCardAgentCell,
    },
    {
      id: "score",
      label: "Score",
      className: classes.tableText,
      formatter: humanReadableDecimal,
    },
  ];

  useEffect(() => {
    setLoading(true);
    Promise.all([getChallenges()]).then(() => {
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    setLoading(true);
    setHasMore(true);
    setChallenges([]);
    Promise.all([getChallenges()])
      .then(() => {
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, [filter]);

  const getChallenges = async (lastKey?: string) => {
    const path = "/game/get-by-search";
    const body = {
      term: clientId,
      column: "CLIENT_ID",
      lastKey: lastKey,
      filter: filter,
    };
    try {
      const data = await API.post(config.apiGateway.NAME, path, {
        body: body,
      });

      if (data) {
        const bodyData = JSON.parse(data.body);
        const challengesData = Array.isArray(bodyData)
          ? bodyData.map((item: any) => item)
          : [];
        if (bodyData.length < 10) {
          setLoading(false);
          setHasMore(false);
        }
        setChallenges((prev) => uniqBy(prev.concat(challengesData), "gameId"));
      }
    } catch (error) {
      setHasMore(false);
      console.log('Error "getChallenges()"', error);
    }
  };
  const lastChallenge = challenges[challenges.length - 1];
  const lastKey = lastChallenge && lastChallenge.gameId;
  const openCreateChallengeModal = () => {
    setCreateModalOpen(true);
  };

  const closeCreateChallengeModal = (refresh: boolean = false) => {
    setCreateModalOpen(false);
    if (refresh) {
      getChallenges();
    }
  };

  const updateGamesList = (actionGameId: string, challenge?: ChallengeType) => {
    // if there is a challenge, it was an update action
    if (challenge) {
      setChallenges((challenges) => {
        return challenges.map((currentGame) => {
          if (currentGame.gameId === actionGameId) {
            return challenge;
          }
          return currentGame;
        });
      });
    } else {
      setChallenges((challenges) =>
        challenges.filter(
          (currentChallenge) => currentChallenge.gameId !== actionGameId
        )
      );
    }
  };

  const openEditChallengeModal = (challengeId: string) => {
    const toEdit: any = challenges.find((c) => c.gameId === challengeId);
    setCurrentChallenge(toEdit);
    setEditModalOpen(true);
  };

  const closeEditChallengeModal = (
    originalGameId: string,
    challenge?: ChallengeType
  ) => {
    if (originalGameId && challenge) {
      updateGamesList(originalGameId, challenge);
    }
    setEditModalOpen(false);
  };

  const deleteChallenge = async () => {
    setLockButton(true);
    try {
      const path = "/game/delete";
      const body = {
        gameId: currentChallenge!.gameId,
      };

      await API.post(config.apiGateway.NAME, path, {
        body: body,
      });

      updateGamesList(currentChallenge!.gameId);
      closeDeleteChallengeModal();
      enqueueSnackbar("Challenge deleted successfully", SUCCESS_TOAST_OPTIONS);
    } catch (e) {
      enqueueSnackbar("Failed to delete challenge", ERROR_TOAST_OPTIONS);
    }
    setLockButton(false);
  };

  const openDeleteChallengeModal = (challengeId: string) => {
    const toDelete: any = challenges.find((c) => c.gameId == challengeId);
    setCurrentChallenge(toDelete);
    setDeleteModalOpen(true);
  };

  const closeDeleteChallengeModal = () => {
    setDeleteModalOpen(false);
  };

  /**
   * this fuction is to find the current state of the challenge
   * @param challenge challenge props
   * @returns the status of the challenge
   */
  const getChallengeStatus = (
    challenge: ChallengeType,
    newChallengeProfile: any
  ) => {
    const startDate = Date.parse(challenge.start_date);
    const now = Date.parse(new Date().toLocaleString());
    const isComplete = challenge.isComplete;
    const winnerProfile = challenge.winnerProfile;

    if (startDate > now) {
      return 0; // return 0 if challenge is new
    }
    if (isComplete && winnerProfile) {
      return 3; // return 3 if challenge have a winner
    } else if (isComplete) {
      return 2; // return 2 if challenge is Draw
    } else {
      return 1; // return 1 if challenge is Active
    }
  };

  const Loading = (
    <div className={classes.loading}>
      <CircularProgress className={classes.CircularProgress} />
    </div>
  );
  const loadMore = async () => {
    setLoading(true);
    await Promise.all([getChallenges(lastKey)]).then(() => {
      setLoading(false);
    });
  };
  const setSelectedFilter = (opt: Filter) => {
    if (opt?.name) {
      setFilter(opt.value);
    }
  };
  const getChallengeItem = (challenge: ChallengeType | null, index: number) => {
    const newChallengeProfile = challenge?.profiles.map((profile) => {
      return {
        ...profile,
        score: profile.score ? parseFloat(profile.score.toFixed(2)) : 0,
      };
    });
    const flip = indicatorsData.find(
      (kpi: Indicator) => challenge?.kpi_id === kpi.entityId
    )?.attributes.flip;
    if (challenge) {
      const rows = flip
        ? formatUsersRows(sortBy(newChallengeProfile, "score"))
        : formatUsersRows(sortBy(newChallengeProfile, "score").reverse());
      return (
        <GameCard key={`challenge-card-${index}`}>
          <GameCard.GameCardHeader
            title={challenge.title}
            status={getChallengeStatus(challenge, newChallengeProfile)}
            coins={challenge.winnerPoints}
            onEdit={() => {
              openEditChallengeModal(challenge.gameId);
            }}
            onDelete={() => {
              openDeleteChallengeModal(challenge.gameId);
            }}
          />
          <GameCard.GameCardMedia title={challenge.title} />
          <GameCard.GameCardContent>
            <GameCard.GameCardStats>
              <GameCard.GameCardStat label="KPI" xs={12} md={12} lg={4}>
                <Typography variant="body2" noWrap>
                  {challenge.kpi_name ? challenge.kpi_name : challenge.title}
                </Typography>
              </GameCard.GameCardStat>
              <GameCard.GameCardStat label="START DATE" xs={6} md={6} lg={4}>
                <>
                  <Typography variant="caption">
                    {humanReadableTimeStamp(new Date(challenge.start_date))}
                  </Typography>
                </>
              </GameCard.GameCardStat>
              <GameCard.GameCardStat label="END DATE" xs={6} md={6} lg={4}>
                <>
                  <Typography variant="caption">
                    {humanReadableTimeStamp(new Date(challenge.end_date))}
                  </Typography>
                </>
              </GameCard.GameCardStat>
            </GameCard.GameCardStats>
            <GameCard.GameCardTable
              data={rows}
              columns={columns}
              status={getChallengeStatus(challenge, newChallengeProfile)}
              gameId={challenge.gameId}
            />
          </GameCard.GameCardContent>
        </GameCard>
      );
    }

    return <ChallengeSkeletonCard key={`challenge-card-skeleton-${index}`} />;
  };

  const handleShowCard = (challenge: any, index: number) => {
    return (
      <Grid item xs={12} sm={6} md={6} lg={4}>
        {getChallengeItem(challenge, index)}
      </Grid>
    );
  };

  return (
    <>
      <Grid container direction="row" className={classes.buttonsSection}>
        <GameFilter onSelect={setSelectedFilter} />
        <Grid item>
          <Can I="create" a="challenges">
            <Button
              variant="contained"
              color="primary"
              onClick={openCreateChallengeModal}
            >
              Create Challenge
            </Button>
          </Can>
        </Grid>
      </Grid>
      <InfiniteScroll
        dataLength={challenges.length}
        next={loadMore}
        hasMore={hasMore}
        loader={Loading}
        scrollableTarget={myRef}
        className={classes.infiniteScroll}
      >
        <Grid container className={classes.root} spacing={2} ref={myRef}>
          <>{React.Children.toArray(challenges.map(handleShowCard))}</>
        </Grid>
        {loading && !lastKey && Loading}
      </InfiniteScroll>
      <Dialog
        open={isCreateModalOpen}
        onClose={() => closeCreateChallengeModal()}
      >
        <UOneDialogTitle
          id="challenge-form-dialog"
          onClose={() => closeCreateChallengeModal()}
        >
          Create Challenge
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <CreateChallengeForm
            refreshFunction={() => closeCreateChallengeModal(true)}
          />
        </UOneDialogContent>
      </Dialog>

      <Dialog
        open={isEditModalOpen}
        onClose={() => closeEditChallengeModal(currentChallenge!.gameId)}
      >
        <UOneDialogTitle
          id="challenge-form-dialog"
          onClose={() => closeEditChallengeModal(currentChallenge!.gameId)}
        >
          Edit Challenge
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <EditChallengeForm
            refreshFunction={(gameId: string, challenge: ChallengeType) =>
              closeEditChallengeModal(gameId, challenge)
            }
            challenge={currentChallenge}
          />
        </UOneDialogContent>
      </Dialog>

      <AcceptCancelModal
        isOpen={isDeleteModalOpen}
        title={"Delete Challenge"}
        text={"Are you sure you want to delete the challenge?"}
        acceptFunc={deleteChallenge}
        cancelFunc={closeDeleteChallengeModal}
        disabled={lockButton}
      />
    </>
  );
};

export default memo(ListChallenges);
