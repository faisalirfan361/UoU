import React from "react";
import { Story, Meta } from "@storybook/react/types-6-0";

import IProps from "./types";

import AvatarComponent from ".";

export default {
  component: AvatarComponent,
  title: "AvatarComponent",
  argTypes: {
    initials: { name: "Initials", control: { type: "text" } },
    src: { name: "Source", control: { type: "text" } },
    alt: { name: "Alternative text", control: { type: "text" } },
    backgroundColor: { name: "Background Color", control: { type: "color" } },
    dimension: { name: "Dimension", control: { type: "number" } },
    extraStyles: { name: "Extra Styles", control: { type: "object" } },
    className: { name: "Class Name", control: { type: "text" } },
  },
} as Meta;

export const avatar: Story<IProps> = (args) => <AvatarComponent {...args} />;
avatar.args = {
  initials: "JS",
  backgroundColor: "#252525",
  dimension: 54,
  extraStyles: {},
};

export const avatarWithImage: Story<IProps> = (args) => (
  <AvatarComponent {...args} />
);

avatarWithImage.args = {
  initials: "JS",
  src: "https://picsum.photos/500",
  alt: "Some text",
  backgroundColor: "#252525",
  dimension: 54,
  extraStyles: {},
  className: "some-other-class",
};
