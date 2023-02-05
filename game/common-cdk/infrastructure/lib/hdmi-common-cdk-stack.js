const cdk = require('aws-cdk-lib');
const s3 = require('aws-cdk-lib/aws-s3');
const { HdmiTagHandler } = require('../../src/hdmi-tag-handler');
const lambda = require('aws-cdk-lib/aws-lambda');
const { RemovalPolicy } = require('aws-cdk-lib');

/**
 * This is the parent for each stack that will be used across HDN
 * We need to make sure each micro service stack extends this 
 * this helps us track better in terms of cost, peformance, logging
 * and an organisational view of detailed logging.
 * Connect with Faisal and confluence for further docs
 * 
 */
class HdMiCommonCDKPackageStack extends cdk.Stack {
    constructor(scope, id, repo, envName, props) {
      super(scope, id, props);
  
      // We need to deploy the package in a stage but we need a resource to build the stage. 
      // This code package doesnt have resources so we built a layer version so we could build the stage, so we could deploy the package.

      const lambdaVersion = new lambda.LayerVersion(this, `common-cdk-layer-${envName}`, {
        removalPolicy: RemovalPolicy.RETAIN,
        code: lambda.Code.fromAsset('../src'),
        compatibleArchitectures: [lambda.Architecture.X86_64, lambda.Architecture.ARM_64],
        layerVersionName: `common-cdk-${envName}`,
        description: "provides the resources to the clients."
      });

      /**
       * DO NOT add follow tags in your stacks or it will fail to deploy
       */
      const hdmiTagHandler = HdmiTagHandler(cdk.Tags, {envName: envName})
      hdmiTagHandler.tag(lambdaVersion,"PROJECT","CommonCDK");
        hdmiTagHandler.tag(lambdaVersion,"ENVIRONMENT",envName);
        hdmiTagHandler.tag(lambdaVersion,"FUNCTIONALITY","SDLC");
        hdmiTagHandler.tag(lambdaVersion,"OWNER","Eng");
    }
}

module.exports = { HdMiCommonCDKPackageStack }