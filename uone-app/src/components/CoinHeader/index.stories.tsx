import React from "react";
import { Story, Meta } from "@storybook/react/types-6-0";
import PetsIcon from "@material-ui/icons/Pets";

import IProps from "./types";

import CoinHeaderComponent from ".";

export default {
  component: CoinHeaderComponent,
  title: "CoinHeaderComponent",
  argTypes: {
    imageUrl: { name: "Image URL", control: { type: "text" } },
    level: { name: "Level", control: { type: "object" } },
    pointsToLevelUp: { name: "Image URL", control: { type: "number" } },
    challengesWon: { name: "Image URL", control: { type: "number" } },
    points: { name: "Image URL", control: { type: "number" } },
    coins: { name: "Image URL", control: { type: "number" } },
  },
} as Meta;

export const coinHeader: Story<IProps> = (args) => (
  <CoinHeaderComponent {...args} />
);
coinHeader.args = {
  imageUrl: "https://picsum.photos/500",
  level: {
    icon: PetsIcon,
    name: "Puppy",
    levelNumber: 1,
  },
  pointsToLevelUp: 12,
  challengesWon: 45,
  points: 1200,
  coins: 666,
};
