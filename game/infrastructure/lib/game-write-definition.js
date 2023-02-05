const cdk = require("aws-cdk-lib");
const lambda = require("aws-cdk-lib/aws-lambda");
const dynamodb = require("aws-cdk-lib/aws-dynamodb");
const { AVAILABLE_ENVS } = require("./available-envs");
const es = require("aws-cdk-lib/aws-elasticsearch");
const iam = require("aws-cdk-lib/aws-iam");
const ec2 = require("aws-cdk-lib/aws-ec2");
const ssm = require("aws-cdk-lib/aws-ssm");

const QUEST_BASE_STACK = "Hdmi-Quest-"
const QUEST_BASE_STACK_SERVICE = QUEST_BASE_STACK + "Service-"
const BASE_STACK = "Hdmi-Games-";
const BASE_STACK_WRITE = BASE_STACK + "Write-";
const ENV_PROD = "prod";
const { HdmiLambda, HdmiVPC, HdmiStackClass, HdmiTagHandler } = require("@hdmi/common-cdk");

const HdmiStack = HdmiStackClass(cdk);

/**
 * GameWriteDefinition this is to create stack for write path
 */
class GameWriteDefinition extends HdmiStack {
    /**
     * @param {cdkContext} cdkContext
     * @param {cdk.Construct} scope
     * @param {string} id
     * @param {cdk.StackProps} props
     */
    constructor(cdkContext, scope, id, props) {
        super(cdkContext, scope, id, props);

        const env = props.env;

        const hdmiVPC = new HdmiVPC(this, ec2);
        let vpcLambda = hdmiVPC.getDefaultHdmiConfigWithVPC(
            cdk,
            props.tags.backboneEnv
        );

        const { envName } = cdkContext;
        const prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");
        const hdmiTagHandler = HdmiTagHandler(cdk.Tags, { envName: envName });

        /**
         * Adding tags for Game Write definition Stack
         */ 
        hdmiTagHandler.tag(this, "PROJECT", "HdMiGames-Write");
        hdmiTagHandler.tag(this, "ENVIRONMENT", envName);
        hdmiTagHandler.tag(this, "OWNER", "Eng");

        let gameTableName = `Hdmi-Games-Table-${prettyEnvName}`
        // following will be replaced by string param fixing import value loop issue
        // cdk.Fn.importValue(
        //     `${BASE_STACK}Game-Table-Name-${envName}`
        // );
        let hdmiLambda = new HdmiLambda(this, lambda, cdk, cdkContext);
        const questUpsertEntitiesFunction = hdmiLambda.fromFunctionAttributes(
            `Hdmi-QuestGateway-ServiceFn-UpsertEntities-${envName}`
        );
        const questUpsertPathsFunction = hdmiLambda.fromFunctionAttributes(
            `Hdmi-QuestGateway-ServiceFn-UpsertPaths-${envName}`
        );
        const questExecuteQueryFunction = hdmiLambda.fromFunctionAttributes(
            `Hdmi-Quest-Service-ExecuteContextQueryHandler-${envName}`
        );
        const questExecuteScriptFunction = hdmiLambda.fromFunctionAttributes(
            `${QUEST_BASE_STACK_SERVICE}ExecuteScriptHandler-${envName}`
        );
        const getEntitiesByIDList = hdmiLambda.fromFunctionAttributes(
            `Hdmi-Entity-Read-Get-Entities-By-Id-List-FN-MS-${envName}`
        );
        /**
         * followig is being used to add access to Event Bus
         */
        let evBusPolicy = {
            effect: iam.Effect.ALLOW,
            resources: ["*"],
            actions: [
                "events:EnableRule",
                "events:PutRule",
                "events:DisableRule",
                "events:PutEvents",
                "events:DescribeRule",
                "events:CreateEventBus",
                "events:DescribeEventBus",
                "events:DeleteRule",
                "events:PutTargets",
                "events:DeleteEventBus",
                "events:RemoveTargets",
            ],
        };

        const gameTable = dynamodb.Table.fromTableName(
            this,
            `${BASE_STACK}Table-${prettyEnvName}`,
            gameTableName
        );
        /**
         * followig is being used to cset environment vairables
         */
        let envData = {
            REGION: props.region,
            GAME_INDEX: props.gameIndex,
            GAME_STATE_INDEX: props.gameStateIndex,
            GAME_TABLE: gameTable.tableName,
            UPSERT_ENTITIES_FN: questUpsertEntitiesFunction.functionName,
            UPSERT_PATH_FN: questUpsertPathsFunction.functionName,
            EXECUTE_QUERY_FN: questExecuteQueryFunction.functionName,
            EXECUTE_SCRIPT_FN: questExecuteScriptFunction.functionName,
            GET_ENTITIES_BY_ID_List_FN: getEntitiesByIDList.functionName
        };

        /**
         * followig is being used to add support for Cron Game Scheduler
         * games in graph
         */
        const gameCronHandler = hdmiLambda.newFunction(
            `${BASE_STACK_WRITE}Cron-Handler-${prettyEnvName}`,
            "src/write/game-write-entrypoint.rest.scheduleGameDetailsHandler",
            envData,
            "scheduling",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );

        let rulesARN = `arn:aws:events:${props.cdkEnv.region}:${props.cdkEnv.account}:rule/*`;
        const servicePrincipal = new iam.ServicePrincipal(
            "events.amazonaws.com"
        );
        const servicePrincipalWithConditions = servicePrincipal.withConditions({
            ArnLike: {
                "aws:SourceArn": rulesARN,
            },
        });

        gameCronHandler.addPermission("Invoke Cron Lambda", {
            principal: servicePrincipal,
            sourceArn: rulesARN,
            sourceAccount: props.cdkEnv.account,
        });

        envData["GAME_CRON_FUNC_ARN"] = gameCronHandler.functionArn;
        /**
         * followig is being used to add Insert Game Lambda
         */
        const insertGameFn = hdmiLambda.newFunction(
            `${BASE_STACK_WRITE}InsertGame-${prettyEnvName}`,
            "src/write/game-write-entrypoint.rest.insertGameHandler",
            envData,
            "references",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );

        /**
         * followig is being used to add Update Game Lambda
         */
        const updateGameFn = hdmiLambda.newFunction(
            `${BASE_STACK_WRITE}UpdateGame-${prettyEnvName}`,
            "src/write/game-write-entrypoint.rest.updateGameHandler",
            envData,
            "references",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );
        
        /**
         *  Lambda Accept Duel
         */
        const acceptDuelFN = hdmiLambda.newFunction(
            `${BASE_STACK_WRITE}AcceptDuel-${prettyEnvName}`,
            "src/write/game-write-entrypoint.rest.acceptDuelMSHandler",
            envData,
            "references",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );
        gameTable.grantReadWriteData(acceptDuelFN);
        
        /**
         * followig is being used to add delete Game Lambda
         */
        const deleteGameFn = hdmiLambda.newFunction(
            `${BASE_STACK_WRITE}DeleteGame-${prettyEnvName}`,
            "src/write/game-write-entrypoint.rest.deleteGameHandler",
            envData,
            "references",
            "HdMiGames",
            {
                ...vpcLambda,
            }
        );
        
        insertGameFn.role.addToPolicy(new iam.PolicyStatement(evBusPolicy));
        updateGameFn.role.addToPolicy(new iam.PolicyStatement(evBusPolicy));
        deleteGameFn.role.addToPolicy(new iam.PolicyStatement(evBusPolicy));
        gameCronHandler.role.addToPolicy(new iam.PolicyStatement(evBusPolicy));
        acceptDuelFN.role.addToPolicy(new iam.PolicyStatement(evBusPolicy));
        /**
         * give permissions on game table in dynamo and game archive table
         */
        gameTable.grantReadWriteData(insertGameFn);
        questExecuteScriptFunction.grantInvoke(insertGameFn)
        getEntitiesByIDList.grantInvoke(insertGameFn)
        gameTable.grantReadWriteData(updateGameFn);
        gameTable.grantReadWriteData(deleteGameFn);
        gameTable.grantReadWriteData(gameCronHandler);

        /**
         * add permissions for game ES
         */
         // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)
        const oldInsertGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}InsertGame-ARN-Parame-${env}`, {
            parameterName: `${BASE_STACK_WRITE}InsertGame-ARN-${env}`,
            stringValue: insertGameFn.functionArn
        });
         const oldUpdateGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}UpdatetGame-ARN-Param-${env}`, {
            parameterName: `${BASE_STACK_WRITE}UpdatetGame-ARN-${env}`,
            stringValue: updateGameFn.functionArn
        });
         const oldDeleteGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}DeleteGame-ARN-Param-${env}`, {
            parameterName: `${BASE_STACK_WRITE}DeleteGame-ARN-${env}`,
            stringValue: updateGameFn.functionArn
        }); 
        new cdk.CfnOutput(this, `${BASE_STACK_WRITE}InsertGame-ARN-${env}`, {
            exportName: `${BASE_STACK_WRITE}InsertGame-ARN-${env}`,
            value: insertGameFn.functionArn,
        });
        new cdk.CfnOutput(this, `${BASE_STACK_WRITE}UpdatetGame-ARN-${env}`, {
            exportName: `${BASE_STACK_WRITE}UpdatetGame-ARN-${env}`,
            value: updateGameFn.functionArn,
        });
        new cdk.CfnOutput(this, `${BASE_STACK_WRITE}DeleteGame-ARN-${env}`, {
            exportName: `${BASE_STACK_WRITE}DeleteGame-ARN-${env}`,
            value: updateGameFn.functionArn,
        }); // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)

        const insertGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}InsertGame-ARN-Parame-${prettyEnvName}`, {
            parameterName: `${BASE_STACK_WRITE}InsertGame-ARN-${prettyEnvName}`,
            stringValue: insertGameFn.functionArn
        });
         const updateGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}UpdatetGame-ARN-Param-${prettyEnvName}`, {
            parameterName: `${BASE_STACK_WRITE}UpdatetGame-ARN-${prettyEnvName}`,
            stringValue: updateGameFn.functionArn
        });
         const deleteGameName = new ssm.StringParameter(this, `${BASE_STACK_WRITE}DeleteGame-ARN-Param-${prettyEnvName}`, {
            parameterName: `${BASE_STACK_WRITE}DeleteGame-ARN-${prettyEnvName}`,
            stringValue: updateGameFn.functionArn
        });

    }
    loadFunctionByARN(context, env, key, funcId) {
        const fnARN = cdk.Fn.importValue(key);
        return lambda.Function.fromFunctionArn(context, funcId, fnARN);
    }
}

/**
 * GameWriteDefinitionStage write definition stck for write stage
 */
class GameWriteDefinitionStage extends cdk.Stage {
    constructor(scope, id, cdkContext, env, writeStackProps, props) {
        super(scope, id, props);

        new GameWriteDefinition(
            cdkContext,
            this,
            `Hdmi-Games-Write-${env}`,
            writeStackProps
        );
    }
}

module.exports = { GameWriteDefinitionStage };
