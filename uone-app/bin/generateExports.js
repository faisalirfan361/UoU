/* eslint-disable */

const path = require("path");
const AWS = require("aws-sdk");
const fs = require("fs");

const REGION = process.env.AWS_REGION || "us-west-2";
AWS.config.update({ region: REGION });

const bucketTargetOutputKey = "BucketTarget";
const cognitoIdentityPoolOutputKey = "CognitoIdentityPool";
const cognitoUserPoolOutputKey = "CognitoUserPool";
const cognitoUserPoolClientOutputKey = "CognitoUserPoolClient";
const webSocketEndpointOutputKey = "WSSURL";

const cloudformation = new AWS.CloudFormation();

async function getCloudFormationOutputs(stackName) {
  const {
    Stacks: [stack],
  } = await cloudformation
    .describeStacks({
      StackName: stackName,
    })
    .promise();

  const mainStackOutputs = stack.Outputs.reduce((finalOutput, output) => {
    finalOutput[output.OutputKey] = output.OutputValue;
    return finalOutput;
  }, {});

  return mainStackOutputs;
}

(async () => {
  try {
    const [stage] = process.argv.slice(2) || [];
    if (!stage) throw new Error("Stage argument not found.");

    const exportFileName = "src/config.ts";

    console.log("Generating config.ts");

    // Main cloudformation outputs
    const mainStackOutputs = await getCloudFormationOutputs(`uone-api-${stage}`);

    // Extract host from endpoint
    const APIG = `${stage}.uone-app.com`;

    // Describe image message stack
    const imageStackOutputs = await getCloudFormationOutputs(
      `image-approve-messages-${stage}`
    );

    const amplifyConfig = {
      reportingDashboardID: "/** ENTER REPORTING DASHBOARD ID **/",
      webSocket: imageStackOutputs[webSocketEndpointOutputKey],
      targetBucketUrl: `https://${mainStackOutputs[bucketTargetOutputKey]}.s3-${REGION}.amazonaws.com/`,
      apiGateway: {
        NAME: "ApiGateway",
        REGION,
        // ServiceEndpoint
        HOST: APIG,
        URL: `https://${APIG}`,
      },
      cognito: {
        REGION,
        USER_POOL_ID: mainStackOutputs[cognitoUserPoolOutputKey],
        APP_CLIENT_ID: mainStackOutputs[cognitoUserPoolClientOutputKey],
        IDENTITY_POOL_ID: mainStackOutputs[cognitoIdentityPoolOutputKey],
      },
    };

    const data = `
    /** GENERATED EXPORTS **/
    export default ${JSON.stringify(amplifyConfig, null, 4)}`;
    fs.writeFileSync(exportFileName, data);
    console.log(`Config file generated: ${exportFileName}`);
  } catch (error) {
    console.log("Error: " + error);
  }
})();
