const { UOneTagHandler } = require('@uone/common-cdk');
const cdk = require('aws-cdk-lib');
const s3 = require('aws-cdk-lib/aws-s3');

/**
 * UOneGamePackageStack base stack for Game read, Write and Scheduler Stacks
 */
class UOneGamePackageStack extends cdk.Stack {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const uoneTagHandler = UOneTagHandler(cdk.Tags, { envName: envName });

    const bucket = new s3.Bucket(this, `GameBucket2504-${envName}`, {
      versioned: true
    });
    uoneTagHandler.tag(bucket, "PROJECT", "UOneGames");
    uoneTagHandler.tag(bucket, "ENVIRONMENT", envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    }).replace(/ /g, ""));
    uoneTagHandler.tag(bucket, "FUNCTIONALITY", "SDLC");
    uoneTagHandler.tag(bucket, "OWNER", "Eng");
  }
}

module.exports = { UOneGamePackageStack }