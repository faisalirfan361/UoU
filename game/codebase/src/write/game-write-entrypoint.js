const AWS = require('aws-sdk')
const {GameConfiguration} = require('../game-configuration')
const {GameWriteRepository} = require('./repositories/game-write-repository')
const {GameWriteService} = require('./services/game-write-service')
const {GameWriteRestHandler} = require('./handlers/game-write-rest-handlers')
const {GameSearchService} = require('../shared/game-search-service')
const {GameSchedulerService} = require('./services/game-schedule-service')
const {WriteQuestGraphService} = require('./services/quest-write-graph-service')
const {GameWriteDynamoHandler} = require('./handlers/game-write-dynamo-handlers')
const {GameWriteSQSHandlers} = require('./handlers/game-write-sqs-handlers')
const {QuestServiceClient} = require('@uone/quest-clients').Service;
const {EntityReadClients} = require('@uone/uone-entity-clients').Read 
const {LambdaInvoker} = require('@heyday/uone-utils').Lambda
const {GameReadService} = require('../read/services/game-read-service')
const {GameReadRepository} = require('../read/repositories/game-read-repository')
const { ReadQuestGraphService } = require('../read/services/game-read-graph-service')

const lambda = new AWS.Lambda()
const lambdaInvoker = LambdaInvoker(lambda)
const entityReadClient = EntityReadClients(lambdaInvoker, GameConfiguration)
const documentClient = new AWS.DynamoDB.DocumentClient()
const clientConfiguration = {
    UpsertEntitiesFunction: process.env.UPSERT_ENTITIES_FN,
    UpsertPathsFunction: process.env.UPSERT_PATH_FN,
    ExecuteContextQueryFunction: process.env.EXECUTE_QUERY_FN,
    DeleteEntityFunction: process.env.DELETE_QUEST_ENTITY,
    ExecuteScriptFunction: process.env.EXECUTE_SCRIPT_FN,
};

const gameReadRepository = GameReadRepository(documentClient, GameConfiguration)

const questServiceClient = QuestServiceClient(lambdaInvoker, clientConfiguration);
const writeQuestGraphService = WriteQuestGraphService(questServiceClient)

const readQuestGraphService = ReadQuestGraphService(questServiceClient)
const gameSearchService = GameSearchService()
const gameWriteRepository = GameWriteRepository(documentClient, GameConfiguration)
const gameSchedulerService = GameSchedulerService(gameWriteRepository, readQuestGraphService, entityReadClient)
const gameWriteService = GameWriteService(gameWriteRepository, gameSearchService, GameConfiguration, gameSchedulerService, entityReadClient, writeQuestGraphService, gameReadRepository, readQuestGraphService)
const gameWriteRestHandlers = GameWriteRestHandler(gameWriteService)
const gameWriteDynamoHandler = GameWriteDynamoHandler(gameWriteService, gameSchedulerService)
const gameWriteSQSHandlers = GameWriteSQSHandlers(gameWriteService)

/**
 * load all dependencies and expose all the handlers for Gateway and clients
 */
module.exports = {
    rest: {
        ...gameWriteRestHandlers
    },
    dynamodb: {
        ...gameWriteDynamoHandler
    },
    sqs: {
        ...gameWriteSQSHandlers
    }
}
