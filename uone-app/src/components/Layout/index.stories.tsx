import React from "react";
import { storiesOf } from "@storybook/react";
import { MenuVariant } from "../../constants";
import { RecoilRoot } from "recoil";
import Layout from ".";

storiesOf("Layout", module)
  .add("basic", () => (
    <RecoilRoot>
      <Layout title="Dashboard" menuVariant={MenuVariant.ADMIN}>
        <h1>Title</h1>
      </Layout>
    </RecoilRoot>
  ))
  .add("second", () => (
    <RecoilRoot>
      <Layout title="Dashboard" menuVariant={MenuVariant.AGENT}>
        <h1>Title1</h1>
      </Layout>
    </RecoilRoot>
  ));
