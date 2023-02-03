interface IChallengeAtomState {
  cognitoID: string,
  setID: string,
  metric: string,
  title: string,
  description: string,
  joinPoints: number,
  winnerPoints: number,
  startDate: Date,
  endDate: Date,
  thumbnailImage: string,
  status: boolean,
  agents?: any[]
}

export default IChallengeAtomState;
