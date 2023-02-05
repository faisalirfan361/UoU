const cdk = require("aws-cdk-lib");
const lambda = require("aws-cdk-lib/aws-lambda");
const ec2 = require("aws-cdk-lib/aws-ec2");
const dynamodb = require("aws-cdk-lib/aws-dynamodb");
const ssm = require("aws-cdk-lib/aws-ssm");
const { AVAILABLE_ENVS } = require("./available-envs");
const es = require("aws-cdk-lib/aws-elasticsearch");
const { GameConfiguration } = require("../../codebase/src/game-configuration");
const BASE_STACK = "UOne-Games-";
const BASE_STACK_READ = BASE_STACK + "Read-";
const QUEST_BASE_STACK = "UOne-Quest-"
const QUEST_BASE_STACK_SERVICE = QUEST_BASE_STACK + "Service-"
const ENV_PROD = "prod";
const iam = require("aws-cdk-lib/aws-iam");
const { DynamoEventSource } = require("aws-cdk-lib/aws-lambda-event-sources");
const sqs = require("aws-cdk-lib/aws-sqs");
const { SqsEventSource } = require("aws-cdk-lib/aws-lambda-event-sources");
const { UOneLambda, UOneVPC, UOneStackClass, UOneTagHandler } = require("@uone/common-cdk");
const UOneStack = UOneStackClass(cdk);

/**
 * We need to change UOneStack so we have support for multi user account right now it will be a big change
 * and for now since we have time limitation we wont be abe to do that.
 */
class GameReadDefinitionStack extends UOneStack {
    /**
     * @param {cdkContext} cdkContext
     * @param {construct} scope
     * @param {string} id
     * @param {cdk.StackProps} props
     */
    constructor(cdkContext, scope, id, envName, props) {
        super(cdkContext, scope, id, props);
        
        const env = props.env;
        const prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");
        const uoneVPC = new UOneVPC(this, ec2);
        let vpcLambda = uoneVPC.getDefaultUOneConfigWithVPC(
            cdk,
            props.tags.backboneEnv
        );

        // const { envName } = cdkContext;
        const uoneTagHandler = UOneTagHandler(cdk.Tags, { envName: envName });
                
        /**
         * Adding tags for Game Read definition Stack
         */ 
        uoneTagHandler.tag(this, "PROJECT", "UOneGames-Read");
        uoneTagHandler.tag(this, "ENVIRONMENT", envName);
        uoneTagHandler.tag(this, "OWNER", "Eng");
        /**
         * followig is being used to add access to Event Bus
         */
        let evBusPolicy = {
            effect: iam.Effect.ALLOW,
            resources: ["*"],
            actions: [
                "events:EnableRule",
                "events:PutRule",
                "events:RemoveTargets",
                "events:DisableRule",
                "events:PutEvents",
                "events:DescribeRule",
                "events:CreateEventBus",
                "events:DescribeEventBus",
                "events:DeleteRule",
                "events:PutTargets",
                "events:DeleteEventBus",
                "events:TagResource",
                "iam:CreateRole",
                "iam:PutRolePolicy",
                "lambda:InvokeAsync",
                "lambda:InvokeFunction",
                "iam:PassRole",
                "lambda:UpdateAlias",
                "lambda:CreateAlias",
                "lambda:GetFunctionConfiguration",
                "lambda:AddPermission",
            ],
        };
        /**
         * Create Game Table in Dynamo DB
         */
        const removalPolicy =
            env === AVAILABLE_ENVS.INTEGRATION
                ? cdk.RemovalPolicy.DESTROY
                : cdk.RemovalPolicy.RETAIN;
        const gameTable = new dynamodb.Table(
            this,
            `${BASE_STACK}Table-${env}`,
            {
                tableName: `${BASE_STACK}Table-${prettyEnvName}`,
                partitionKey: {
                    name: "gameId",
                    type: dynamodb.AttributeType.STRING,
                },
                billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
                removalPolicy: removalPolicy,
                stream: dynamodb.StreamViewType.NEW_AND_OLD_IMAGES,
            }
        );
        uoneTagHandler.tag(gameTable, "FUNCTIONALITY", "references");

        /**
         * adds indexes in dynamodb
         */
        gameTable.addGlobalSecondaryIndex({
            indexName: GameConfiguration.GameIndexes.SEARCH_BY_CLIENT_ID,
            partitionKey: {
                name: "clientId",
                type: dynamodb.AttributeType.STRING,
            },
        });

        gameTable.addGlobalSecondaryIndex({
            indexName:
                GameConfiguration.GameIndexes
                    .SEARCH_BY_CLIENT_ID_WITH_CREATED_AT,
            partitionKey: {
                name: "clientId",
                type: dynamodb.AttributeType.STRING,
            },
            sortKey: {
                name: "createdAt",
                type: dynamodb.AttributeType.NUMBER,
            },
        });

        let uoneLambda = new UOneLambda(this, lambda, cdk, cdkContext);
        /**
         * load `UOne-QuestGateway-ServiceFn-UpsertEntities-${envName}` to upsertEntities in neptune
         */
        const questUpsertEntitiesFunction = uoneLambda.fromFunctionAttributes(
            `UOne-QuestGateway-ServiceFn-UpsertEntities-${envName}`
        );
        /**
         *  `UOne-QuestGateway-ServiceFn-UpsertPaths-${envName}` to create paths between graph/neptune node
         */
        const questUpsertPathsFunction = uoneLambda.fromFunctionAttributes(
            `UOne-QuestGateway-ServiceFn-UpsertPaths-${envName}`
        );
        /**
         *  `UOne-Quest-Service-ExecuteContextQueryHandler-${envName}` Quest Query handler
         */
        const questExecuteQueryFunction = uoneLambda.fromFunctionAttributes(
            `UOne-Quest-Service-ExecuteContextQueryHandler-${envName}`
        );
        /**
         * `UOne-QuestGateway-ServiceFn-DeleteEntity-${envName}` Delete Entity Gateway reference
         */
        const questDeleteEntityFunction = uoneLambda.fromFunctionAttributes(
            `UOne-QuestGateway-ServiceFn-DeleteEntity-${envName}`
        );
        /**
         * `UOne-Games-Write-Cron-Handler-${prettyEnvName}` Delete Entity Gateway reference
         */
        const gameCronHandler = uoneLambda.fromFunctionAttributes(
            `UOne-Games-Write-Cron-Handler-${prettyEnvName}`
        );
        /**
         * `${QUEST_BASE_STACK_SERVICE}ExecuteScriptHandler-${envName}` Quest scripts executor
         */
        const questExecuteScriptFunction = uoneLambda.fromFunctionAttributes(
            `${QUEST_BASE_STACK_SERVICE}ExecuteScriptHandler-${envName}`
        );
        /**
         * `UOne-Entity-Read-Get-Entities-By-Id-List-FN-MS-${envName}`Read entity reference
         */
        const getEntitiesByIDList = uoneLambda.fromFunctionAttributes(
            `UOne-Entity-Read-Get-Entities-By-Id-List-FN-MS-${envName}`
        );
        /**
         * `UOne-Games-Edges-SQS-Events-${prettyEnvName}`Quest SQS Write path
         */
        const gameEdgesSQSQueueName = `UOne-Games-Edges-SQS-Events-${prettyEnvName}`;

        /**
         * `UOne-Games-Edges-SQS-EventsDLQ-${prettyEnvName}`
         * SQS for writing game edges (we use it for edges between games, groups, users, kpis and metrics)
         */
        const gameEdgeDLQ = new sqs.Queue(
            this,
            `UOne-Games-Edges-SQS-EventsDLQ-${prettyEnvName}`,
            {
                queueName: `UOne-Games-Edges-SQS-EventsDLQ-${prettyEnvName}`,
                retentionPeriod: cdk.Duration.days(14),
            }
        );
        uoneTagHandler.tag(gameEdgeDLQ, "FUNCTIONALITY", "references");

        const gameEdgesSQSQueue = new sqs.Queue(this, gameEdgesSQSQueueName, {
            queueName: gameEdgesSQSQueueName,
            deadLetterQueue: {
                queue: gameEdgeDLQ,
                maxReceiveCount: 3,
            },
        });
        uoneTagHandler.tag(gameEdgesSQSQueue, "FUNCTIONALITY", "references");

        /**
         * environment variables for lambdas
         */
        let envs = {
            REGION: props.region,
            GAME_INDEX: props.gameIndex,
            GAME_STATE_INDEX: props.gameStateIndex,
            GAME_TABLE: gameTable.tableName,
            READ_TABLE: gameTable.tableName,
            UPSERT_ENTITIES_FN: questUpsertEntitiesFunction.functionName,
            UPSERT_PATH_FN: questUpsertPathsFunction.functionName,
            GAME_CRON_FUNC_ARN: gameCronHandler.functionArn,
            EXECUTE_QUERY_FN: questExecuteQueryFunction.functionName,
            EDGES_QUEUE_URL: gameEdgesSQSQueue.queueUrl,
            DELETE_QUEST_ENTITY: questDeleteEntityFunction.functionName,
            EXECUTE_SCRIPT_FN: questExecuteScriptFunction.functionName,
            PRETTY_ENV_NAME: prettyEnvName,
            GET_ENTITIES_BY_ID_List_FN: getEntitiesByIDList.functionName

        };

        /**
         * followig is being used to call dynamo trigger and create lamdba for it to store
         * games in graph this is a read stack but since cdk wont allow to  add stream in other
         * stack then the table was created in so we have to add it here
         */
        const gameDynamoTriggerHandler = uoneLambda.newFunction(
            `${BASE_STACK_READ}Graphs-Insert-Handler-${prettyEnvName}`,
            "src/write/game-write-entrypoint.dynamodb.upsertGameInGraphHandler",
            envs,
            "references",
            "UOneGames",
            {
                timeout: cdk.Duration.seconds(600),
                ...vpcLambda,
            }
        );

        gameDynamoTriggerHandler.role.addToPolicy(
            new iam.PolicyStatement(evBusPolicy)
        );
        /**
         * Add event source for Graph-Insert-Handle
         */
        gameDynamoTriggerHandler.addEventSource(
            new DynamoEventSource(gameTable, {
                startingPosition: lambda.StartingPosition.LATEST,
            })
        );
        gameEdgesSQSQueue.grantSendMessages(gameDynamoTriggerHandler);
        gameTable.grantReadWriteData(gameDynamoTriggerHandler);
        getEntitiesByIDList.grantInvoke(gameDynamoTriggerHandler)
        questUpsertEntitiesFunction.grantInvoke(gameDynamoTriggerHandler);
        questUpsertPathsFunction.grantInvoke(gameDynamoTriggerHandler);
        questDeleteEntityFunction.grantInvoke(gameDynamoTriggerHandler);

        /**
         * followig is being used to call sqs trigger and create lamdba for it to store
         * edges in graph
         */
        const gameUpsertEdgesSQSTriggerHandler = uoneLambda.newFunction(
            `${BASE_STACK}Edges-Upsert-Handler-${prettyEnvName}`,
            "src/write/game-write-entrypoint.sqs.upsertEdgesIntoGraphHandler",
            envs,
            "references",
            "UOneGames",
            {
                ...vpcLambda,
            }
        );

        /**
         * Add event source for Edges-Insert-Handle
         */
        gameUpsertEdgesSQSTriggerHandler.addEventSource(
            new SqsEventSource(gameEdgesSQSQueue, {
                batchSize: 1,
            })
        );
        gameTable.grantReadWriteData(gameUpsertEdgesSQSTriggerHandler);
        questUpsertEntitiesFunction.grantInvoke(
            gameUpsertEdgesSQSTriggerHandler
        );
        questUpsertPathsFunction.grantInvoke(gameUpsertEdgesSQSTriggerHandler);

        /**
         * game games by search lambda
         */
        const getGamesBySearch = uoneLambda.newFunction(
            `${BASE_STACK_READ}Get-Games-By-Search-${prettyEnvName}`,
            "src/read/game-read-entrypoint.rest.getGamesBySearchHandler",
            envs,
            "read-data",
            "UOneGames",
            {
                ...vpcLambda,
            }
        );
        gameTable.grantReadWriteData(getGamesBySearch);

        /**
         * game games by search lambda
         */
        const getGamesByIdsHandlerFN = uoneLambda.newFunction(
            `${BASE_STACK_READ}Get-Games-By-ID-List-${prettyEnvName}`,
            "src/read/game-read-entrypoint.rest.getGamesByIdsHandler",
            envs,
            "references",
            "UOneGames",
            {
                ...vpcLambda,
            }
        );
        gameTable.grantReadWriteData(getGamesByIdsHandlerFN);

        getGamesBySearch.addToRolePolicy(
            new iam.PolicyStatement({
                actions: ["dynamodb:Query"],
                resources: [`${gameTable.tableArn}/index/*`],
            })
        );
        
        // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)
        const oldGameTableNameParam = new ssm.StringParameter(this, `${BASE_STACK}Game-Table-Name-Param-${envName}`, {
            parameterName: `${BASE_STACK}Game-Table-Name-${envName}`,
            stringValue: gameTable.tableName
        })
        new cdk.CfnOutput(this, `${BASE_STACK}Game-Table-Name-${envName}`, {
            exportName: `${BASE_STACK}Game-Table-Name-${envName}`,
            value: gameTable.tableName,
        }); // !! DELETE ME AFTER metrics api starts using proper stack names (with Camal Case environment name)

        // This param should stay and be used in other stacks to import the Game-Table Name
        const gameTableNameParam = new ssm.StringParameter(this, `${BASE_STACK}Game-Table-Name-Param-${prettyEnvName}`, {
            parameterName: `${BASE_STACK}Game-Table-Name-${prettyEnvName}`,
            stringValue: gameTable.tableName
        })
    }
}

/**
 * GameReadDefinitionStage Definition stage for the stack
 */
class GameReadDefinitionStage extends cdk.Stage {
    constructor(scope, id, cdkContext, envName, readStackProps, props) {
        super(scope, id, {
            terminationProtection: !cdkContext.volatile,
            ...props,
            env: cdkContext.envObj
        });

        new GameReadDefinitionStack(
            cdkContext,
            this,
            `UOne-Games-Read-${envName}`,
            envName,
            readStackProps
        );
    }
}

module.exports = { GameReadDefinitionStage };
