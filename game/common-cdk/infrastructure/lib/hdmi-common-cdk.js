#!/usr/bin/env node
const cdk = require('aws-cdk-lib');
const { HdMiCommonCDKPackageStack } = require('./hdmi-common-cdk-stack');
/**
 * TODO @Adam and @Faisal
 * please make sure all CI/CD stages use this as parent
 */
class HdMiCommonCDKPackageStage extends cdk.Stage {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const hdmiCommonStack = new HdMiCommonCDKPackageStack(this, "HdMi-Common-CDK-Package-Stack", repo, envName, props)
  }
}
module.exports = { HdMiCommonCDKPackageStage }