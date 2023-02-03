import {
  ChallengeUserProfileType,
  GameRankUser,
} from "components/GameCard/type";
import { index } from "mathjs";

const MIN_USERS_LENGTH = 3;

export const formatUsersRows = (
  users: ChallengeUserProfileType[]
): GameRankUser[] => {
  const data = users.map((item, index) => ({
    rank: index + 1,
    fullName: `${item.firstName} ${item.lastName}`,
    score: item.score ?? 0,
    avatar: item.profileImg,
  }));

  if (data.length < MIN_USERS_LENGTH) {
    const newArr = new Array(Math.max(MIN_USERS_LENGTH - data.length)).fill(
      null
    );
    return [...data, ...newArr];
  }

  return data;
};
