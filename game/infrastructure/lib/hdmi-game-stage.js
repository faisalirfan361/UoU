#!/usr/bin/env node
const cdk = require('aws-cdk-lib');
const { UOneGamePackageStack } = require('./uone-game-stack');

/**
 * UOneGamePackageStage root stage for Game read, Write and Scheduler Stacks
 */
class UOneGamePackageStage extends cdk.Stage {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const uoneGameStack = new UOneGamePackageStack(this, "UOne-Games-Package-Stack", repo, envName, props)
  }
}
module.exports = { UOneGamePackageStage }