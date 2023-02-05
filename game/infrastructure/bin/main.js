#!/usr/bin/env node
const cdk = require("aws-cdk-lib");
const { UOneGameProject } = require("../lib/project");

const app = new cdk.App();

const environments = new Map();
environments.set("sbx-micro", {
    account: `${SBX_MICRO_ACCOUNT_ID}`,
    region: "us-west-2",
    branch: "sbx-micro",
    backboneEnv: "sbx",
});
environments.set("dev", {
    account: `${DEV_ACCOUNT_ID}`,
    region: "us-west-2",
    branch: "development",
    backboneEnv: "dev",
});
environments.set("shared-services", {
    account: `${SHARED_ACCOUNT_ID}`,
    region: "us-west-2",
});

// sbx-micro pipeline
const sbxMicroProject = new UOneGameProject(app, "UOne-Games-Project-SbxMicro", 'sbx-micro', environments);
// dev pipeline
const devMicroProject = new UOneGameProject(app, "UOne-Games-Project-Dev", 'dev', environments);
