import React from "react";
import { Story, Meta } from "@storybook/react/types-6-0";

import IProps from "./types";

import CoinStoreItemComponent from ".";

export default {
  component: CoinStoreItemComponent,
  title: "CoinStoreItemComponent",
  argTypes: {
    imageUrl: { name: "Image URL", control: { type: "text" } },
    title: { name: "Image URL", control: { type: "text" } },
    description: { name: "Image URL", control: { type: "text" } },
    points: { name: "Image URL", control: { type: "number" } },
  },
} as Meta;

export const coinHeader: Story<IProps> = (args) => (
  <CoinStoreItemComponent {...args} />
);
coinHeader.args = {
  imageUrl:
    "https://cdn.luxe.digital/media/2020/06/08173418/best-earbuds-apple-airpods-pro-luxe-digital%402x.jpg",
  title: "EAR BUDS",
  description:
    "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Expedita perspiciatis dolores velit? Modi architecto quia cumque tempore corrupti omnis laboriosam sit quidem voluptates placeat. Quaerat, laborum nemo. Quisquam, tempora laboriosam!",
  points: 999,
  onClick: () => console.log("GET ITEM clicked!"),
};
