import React from "react";
import { storiesOf } from "@storybook/react";

import { AvatarComponent } from "components";

import CardSimpleComponent from ".";

storiesOf("CardSimpleComponent", module).add("basic", () => (
  <CardSimpleComponent
    singleAvatar={<AvatarComponent alt="alt text" src="singleAvatar" />}
    statusColor="#66D7F9"
    title="Team Stark"
    subtitle="John Snow"
    points={96}
  />
));
