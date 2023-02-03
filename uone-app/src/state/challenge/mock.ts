import IChallengeAtomState from "./types";

export const challenges: IChallengeAtomState[] = [
  {
    cognitoID: "",
    setID: "",
    metric: "",
    title: "",
    description: "",
    joinPoints: 0,
    winnerPoints: 0,
    startDate: new Date(),
    endDate: new Date(),
    thumbnailImage: "",
    status: false,
    agents: []
  }
];
