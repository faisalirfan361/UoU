const { HdmiTagHandler } = require('@hdmi/common-cdk');
const cdk = require('aws-cdk-lib');
const s3 = require('aws-cdk-lib/aws-s3');

/**
 * HdMiGamePackageStack base stack for Game read, Write and Scheduler Stacks
 */
class HdMiGamePackageStack extends cdk.Stack {
  constructor(scope, id, repo, envName, props) {
    super(scope, id, props);

    const hdmiTagHandler = HdmiTagHandler(cdk.Tags, { envName: envName });

    const bucket = new s3.Bucket(this, `GameBucket2504-${envName}`, {
      versioned: true
    });
    hdmiTagHandler.tag(bucket, "PROJECT", "HdMiGames");
    hdmiTagHandler.tag(bucket, "ENVIRONMENT", envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    }).replace(/ /g, ""));
    hdmiTagHandler.tag(bucket, "FUNCTIONALITY", "SDLC");
    hdmiTagHandler.tag(bucket, "OWNER", "Eng");
  }
}

module.exports = { HdMiGamePackageStack }