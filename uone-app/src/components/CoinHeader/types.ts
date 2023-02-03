import React from 'react';

interface Level {
  icon: React.ReactNode;
  levelNumber: number;
  name: string;
}

export interface IProps {
  imageUrl?: string;
  level?: Level;
  pointsToLevelUp?: number;
  challengesWon?: number;
  points?: number;
  coins?: number;
}

export default IProps;
