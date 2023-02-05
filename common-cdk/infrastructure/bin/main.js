#!/usr/bin/env node
const cdk = require('aws-cdk-lib');
const {HdMiCommonCDKProject} = require("../lib/project");


const app = new cdk.App();
const environments = new Map();
environments.set('sbx-micro', { account: '677179051929', region: 'us-west-2' })
environments.set('shared-services', { account: '972576019456', region: 'us-west-2' })

// main pipeline stack
new HdMiCommonCDKProject(app, "Common-CDK-Pipeline-Stack", environments);
