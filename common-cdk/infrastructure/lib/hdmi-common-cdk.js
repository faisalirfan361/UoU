#!/usr/bin/env node
const cdk = require('aws-cdk-lib');
const { UOneCommonCDKPackageStack } = require('./uone-common-cdk-stack');
/**
 * TODO @Adam and @Faisal
 * please make sure all CI/CD stages use this as parent
 */
class UOneCommonCDKPackageStage extends cdk.Stage {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const uoneCommonStack = new UOneCommonCDKPackageStack(this, "UOne-Common-CDK-Package-Stack", repo, envName, props)
  }
}
module.exports = { UOneCommonCDKPackageStage }