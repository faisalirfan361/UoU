const cdk = require("aws-cdk-lib");
const lambda = require("aws-cdk-lib/aws-lambda");
const ec2 = require("aws-cdk-lib/aws-ec2");
const dynamodb = require("aws-cdk-lib/aws-dynamodb");
const { AVAILABLE_ENVS } = require("./available-envs");
const es = require("aws-cdk-lib/aws-elasticsearch");
const targets = require("aws-cdk-lib/aws-events-targets");
const events = require("aws-cdk-lib/aws-events");
const { DynamoEventSource } = require("aws-cdk-lib/aws-lambda-event-sources");
const { HdmiLambda, HdmiVPC, HdmiStackClass, HdmiTagHandler } = require("@hdmi/common-cdk");
const ssm = require("aws-cdk-lib/aws-ssm");
const cxschema = require("aws-cdk-lib/cloud-assembly-schema");

const BASE_STACK = "Hdmi-Games-";
const BASE_STACK_READ = BASE_STACK + "Read-";
const BASE_STACK_SCHEDULER = "Hdmi-Games-Scorer-";
const ENTITY_BASE_STACK_READ = "Hdmi-Entity-Read-";
const SERVICE_CALC_FN = "arn:Hdmi-Calculator-ServiceFn-";
const iam = require("aws-cdk-lib/aws-iam");

const HdmiStack = HdmiStackClass(cdk);

class GameSchedulerDefinition extends HdmiStack {
    /**
     * @param {cdkContext} cdkContext
     * @param {cdk.Construct} scope
     * @param {string} id
     * @param {cdk.StackProps} props
     */
    constructor(cdkContext, scope, id, props) {
        super(cdkContext, scope, id, props);

        const { envName } = cdkContext;
        const prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");
        
        const hdmiVPC = new HdmiVPC(this, ec2);
        let vpcLambda = hdmiVPC.getDefaultHdmiConfigWithVPC(
            cdk,
            props.tags.backboneEnv
        );
        const hdmiTagHandler = HdmiTagHandler(cdk.Tags, { envName: envName });
        const hdmiLambda = HdmiLambda(this, lambda, cdk, cdkContext);
        /**
         * Adding tags for Game Schedulers definition Stack
         */ 
        hdmiTagHandler.tag(this, "PROJECT", "HdMiGames-Schedulers");
        hdmiTagHandler.tag(this, "ENVIRONMENT", envName);
        hdmiTagHandler.tag(this, "OWNER", "Eng");
        const restFnCalculateHandlerArn = this.generateLambdaARNByName(
            cdkContext,
            `Hdmi-Calculator-ServiceFn-CalculateHandler-${envName}`
        );
        const restFnCalculateHandler = lambda.Function.fromFunctionArn(
            this,
            `${SERVICE_CALC_FN}CalculateHandler-${envName}`,
            restFnCalculateHandlerArn
        );
        const getEntityListByTypeFN = hdmiLambda.fromFunctionAttributes(`Hdmi-Entity-Read-GetEntityListByTypeNoCognito-FN-MS-${envName}`)

        // this temporary will be reverted today we need this to fix CICD for entity
        const getAllEntitiesByTypeNClientNoCognitoFN = hdmiLambda.fromFunctionAttributes(`Hdmi-Entity-Read-GetAllByTypeNClientNoCogMS-FN-MS-${envName}`)

        // this.loadFunctionByARN(
        //     this,
        //     envName,
        //     `${ENTITY_BASE_STACK_READ}GetAllEntitiesByTypeNClientNoCognitoMS-FN-ARN-${envName}`,
        //     `${ENTITY_BASE_STACK_READ}GetAllEntitiesByTypeNClientNoCognitoMS-FN-ARN-${envName}`
        // );
        const getMetricByClientCodeAndMetricCodeFN =
            lambda.Function.fromFunctionName(
                this,
                `metric-data-${envName}-getMetricByClientCodeAndMetricCode`,
                `metric-data-${envName}-getMetricByClientCodeAndMetricCode`
            );
        const removalPolicy =
            envName === AVAILABLE_ENVS.INTEGRATION
                ? cdk.RemovalPolicy.DESTROY
                : cdk.RemovalPolicy.RETAIN;

        /**
         * adding indexed to make search better
         */
        // following will be replaced by string param fixing import value loop issue
        let gameTableName = `Hdmi-Games-Table-${prettyEnvName}`
        // following will be replaced by string param fixing import value loop issue
        // cdk.Fn.importValue(
        //     `${BASE_STACK}Game-Table-Name-${envName}`
        // );
        const gameTable = dynamodb.Table.fromTableName(
            this,
            `${BASE_STACK}Table-${prettyEnvName}`,
            gameTableName
        );

        /**
         * user Performance table so we can store and retrieve data for user performance better
         */
        const userPerformanceTable = new dynamodb.Table(
            this,
            `${BASE_STACK}User-Performance-Table-${prettyEnvName}`,
            {
                tableName: `${BASE_STACK}User-Performance-Table-${prettyEnvName}`,
                partitionKey: {
                    name: "userId",
                    type: dynamodb.AttributeType.STRING,
                },
                billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
                stream: dynamodb.StreamViewType.NEW_AND_OLD_IMAGES,
                removalPolicy: removalPolicy,
            }
        );
        userPerformanceTable.addGlobalSecondaryIndex({
            indexName:
                AVAILABLE_ENVS.UserPerformanceTableIndexes
                    .ByDepartmentAndUserId,
            partitionKey: {
                name: "departmentId",
                type: dynamodb.AttributeType.STRING,
            },
            sortKey: { name: "userId", type: dynamodb.AttributeType.STRING },
        });
        hdmiTagHandler.tag(userPerformanceTable, "FUNCTIONALITY", "read-data");

        /**
         * Environment vairbles to be added to lambda functions
         */
        let envs = {
            GAME_STATE_SEARCH_DOMAIN: getEntityListByTypeFN.functionName,
            GET_ENTITIES_BY_TYPE: getEntityListByTypeFN.functionName,
            GET_ALL_ENTITIES_BY_TYPE_N_CLIENT_NO_COGNITO_FN:
                getAllEntitiesByTypeNClientNoCognitoFN.functionName,
            REGION: props.region,
            GAME_INDEX: props.gameIndex,
            GAME_TABLE: gameTable.tableName,
            ReadTable: gameTable.tableName,
            USER_PERFORMANCE_TABLE: userPerformanceTable.tableName,
            CALCULATOR_CALCULATE_FN: restFnCalculateHandler.functionName,
            GET_METRIC_BY_CLIENT_CODE_AND_METRIC_CODE:
                getMetricByClientCodeAndMetricCodeFN.functionName,
        };

        /**
         * GetUserPerformanceDataByuserId get user perofrmance data by ID
         */
        const getUserPerformanceHandler = hdmiLambda.newFunction(
            `${BASE_STACK_SCHEDULER}GetUserPerformanceDataByuserId-MS-${prettyEnvName}`,
            "src/scheduler/game-scheduler-entrypoint.rest.getUserPerformanceDataByuserId",
            envs,
            "read-data",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );
        userPerformanceTable.grantReadWriteData(getUserPerformanceHandler);
        getUserPerformanceHandler.addToRolePolicy(
            new iam.PolicyStatement({
                actions: ["dynamodb:Query"],
                resources: [`${gameTable.tableArn}/index/*`],
            })
        );

        // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)
        new cdk.CfnOutput(
            this,
            `${BASE_STACK_SCHEDULER}GetUserPerformanceDataByuserId-FN-Name-${envName}`,
            {
                exportName: `${BASE_STACK_SCHEDULER}GetUserPerformanceDataByuserId-FN-Name-${envName}`,
                value: getUserPerformanceHandler.functionName,
            }
        );
        new cdk.CfnOutput(this, `Dynamodb-User-Performance-Stream-${envName}`, {
            exportName: `Dynamodb-User-Performance-Stream-${envName}`,
            value: userPerformanceTable.tableStreamArn,
        }); // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)


        const getUserPerfFuncName = new ssm.StringParameter(this, `${BASE_STACK}Game-Table-Name-Param-${prettyEnvName}`, {
            parameterName: `${BASE_STACK_SCHEDULER}GetUserPerformanceDataByuserId-FN-Name-${prettyEnvName}`,
            stringValue: getUserPerformanceHandler.functionName
        });
        const performanceStreamFuncNameParam = new ssm.StringParameter(this, `Dynamodb-User-Performance-Stream-Param-${prettyEnvName}`, {
            parameterName: `Dynamodb-User-Performance-Stream-${prettyEnvName}`,
            stringValue: userPerformanceTable.tableStreamArn
        });
    }
    /**
     * utility function to generate lambda function
     * @param obj context
     * @param str envName
     * @param str key
     * @param str funcId
     * @returns
     */
    loadFunctionByARN(context, envName, key, funcId) {
        const fnARN = cdk.Fn.importValue(key);
        return lambda.Function.fromFunctionArn(context, funcId, fnARN);
    }

    //This function is used to get ARN by name
    generateLambdaARNByName(cdkContext, resourceName) {
        return `arn:aws:lambda:${cdkContext.envObj.region}:${cdkContext.envObj.account}:function:${resourceName}`;
    }
}

/**
 * GameSchedulerDefinitionStage add stage for Game Scheduler definition
 */
class GameSchedulerDefinitionStage extends cdk.Stage {
    constructor(scope, id, cdkContext, envName, writeStackProps, props) {
        super(scope, id, props);
        const prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");

        new GameSchedulerDefinition(
            cdkContext,
            this,
            `Hdmi-Games-Scheduler-${prettyEnvName}`,
            writeStackProps
        );
    }
}

module.exports = { GameSchedulerDefinitionStage };
