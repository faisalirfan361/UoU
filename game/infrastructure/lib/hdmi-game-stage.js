#!/usr/bin/env node
const cdk = require('aws-cdk-lib');
const { HdMiGamePackageStack } = require('./hdmi-game-stack');

/**
 * HdMiGamePackageStage root stage for Game read, Write and Scheduler Stacks
 */
class HdMiGamePackageStage extends cdk.Stage {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const hdmiGameStack = new HdMiGamePackageStack(this, "HdMi-Games-Package-Stack", repo, envName, props)
  }
}
module.exports = { HdMiGamePackageStage }