const { GameSearchService } = require("../../../src/shared/game-search-service")
const { GameWriteRepository } = require("../../../src/write/repositories/game-write-repository")
const { GameSchedulerService } = require("../../../src/write/services/game-schedule-service")
const { updateGame, GameWriteService } = require("../../../src/write/services/game-write-service")
const { GameConfiguration } = require('../../../src/game-configuration')
const AWS = require('aws-sdk')
const games_large = require('../../read/game/data/games_large.json');
const { QuestServiceClient } = require("@uone/quest-clients/src/quest-service-client")
const { GameReadRepository } = require("../../../src/read/repositories/game-read-repository")
const { WriteQuestGraphService } = require("../../../src/write/services/quest-write-graph-service")
const { EntityReadClients } = require('@uone/uone-entity-clients').Read
const { LambdaInvoker } = require('@heyday/uone-utils').Lambda
const { v4: uuid } = require("uuid");

describe('GameWriteRestHandler', () => {
    const lambda = new AWS.Lambda()

    const lambdaInvoker = LambdaInvoker(lambda)
    const entityReadClient = EntityReadClients(lambdaInvoker, GameConfiguration)
    const { generateTestDocumentClient } = require('../../test-utils')
    let documentClient = generateTestDocumentClient()
    const clientConfiguration = {
        UpsertEntitiesFunction: process.env.UPSERT_ENTITIES_FN,
        UpsertPathsFunction: process.env.UPSERT_PATH_FN,
        ExecuteContextQueryFunction: process.env.EXECUTE_QUERY_FN,
        DeleteEntityFunction: process.env.DELETE_QUEST_ENTITY
    };
    const questServiceClient = QuestServiceClient(lambdaInvoker, clientConfiguration);
    const gameReadRepository = GameReadRepository(documentClient, GameConfiguration)

    const writeQuestGraphService = WriteQuestGraphService(questServiceClient)
    const gameSearchService = GameSearchService()
    const gameSchedulerService = GameSchedulerService()
    const gameWriteRepository = GameWriteRepository(documentClient, GameConfiguration)
    const gameWriteService = GameWriteService(gameWriteRepository, gameSearchService, GameConfiguration, gameSchedulerService, entityReadClient, writeQuestGraphService, gameReadRepository)

    let readConfiguration = {
        ReadTable: 'GameTable'
    }
    const OLD_ENV = process.env;
    jest.setTimeout(30000)

    beforeEach(() => {
        jest.resetModules()
        process.env = { ...OLD_ENV };
    });

    afterAll(() => {
        process.env = OLD_ENV;
    });
    it('should update the game if duel is declined', async () => {
        const allData = [...new Set([...games_large])]
        // insert data in dynamo
        for (let i = 0; i < allData.length; i++) {
            await documentClient.put({ TableName: readConfiguration.ReadTable, Item: allData[i] }).promise();
        }
        let gameObj = { isDuel: true, isDeclined: true, gameId: "1234" };
        const resp = await gameWriteService.updateGame(gameObj);
        expect(resp).toEqual({ isDuel: true, isDeclined: true, gameId: "1234", isArchived: true, isComplete: undefined, })
    })

    it('should archive the duel if its declined', async () => {
        const allData = [...new Set([...games_large])]
        // insert data in dynamo
        for (let i = 0; i < allData.length; i++) {
            await documentClient.put({ TableName: readConfiguration.ReadTable, Item: allData[i] }).promise();
        }
        let gameObj = { isDuel: true, isDeclined: true, gameId: "1234" };
        const resp = await gameWriteService.updateGame(gameObj);
        expect(resp.isArchived).toEqual(true)
    })

    it('should update the game and add profiles', async () => {
        const allData = [...new Set([...games_large])]
        // insert data in dynamo
        for (let i = 0; i < allData.length; i++) {
            await documentClient.put({ TableName: readConfiguration.ReadTable, Item: allData[i] }).promise();
        }
        let gameObj = { isDuel: true, isDeclined: false, gameId: "1234", challengesUser: [{ last_name: "Tipiani", team_id: "greenix-group-a78fec01-7f98-4574-ab73-7f71a508565f", user_id: "greenix-user-60b120fd-7dc3-4763-88d9-be54e2f43f76", first_name: "Kelsey" }, { last_name: "Dantes-Castillo", team_id: "greenix-group-f6761aae-59a0-4056-983d-a5bd55a12414", user_id: "greenix-user-235992af-a432-4782-9099-0d2e843f973a", first_name: "Joel" }] };
        const resp = await gameWriteService.updateGame(gameObj);
        expect(resp.profiles).toEqual([{ lastName: "Tipiani", departmentId: "greenix-group-a78fec01-7f98-4574-ab73-7f71a508565f", entityId: "greenix-user-60b120fd-7dc3-4763-88d9-be54e2f43f76", firstName: "Kelsey", score: 0 }, { lastName: "Dantes-Castillo", departmentId: "greenix-group-f6761aae-59a0-4056-983d-a5bd55a12414", entityId: "greenix-user-235992af-a432-4782-9099-0d2e843f973a", firstName: "Joel", score: 0 }])
    })

    it('should not archive the duel if its accepted', async () => {
        const allData = [...new Set([...games_large])]
        // insert data in dynamo
        for (let i = 0; i < allData.length; i++) {
            await documentClient.put({ TableName: readConfiguration.ReadTable, Item: allData[i] }).promise();
        }
        let gameObj = { isDuel: true, isDeclined: false, gameId: "1234", challengesUser: [{ last_name: "Tipiani", team_id: "greenix-group-a78fec01-7f98-4574-ab73-7f71a508565f", user_id: "greenix-user-60b120fd-7dc3-4763-88d9-be54e2f43f76", first_name: "Kelsey" }, { last_name: "Dantes-Castillo", team_id: "greenix-group-f6761aae-59a0-4056-983d-a5bd55a12414", user_id: "greenix-user-235992af-a432-4782-9099-0d2e843f973a", first_name: "Joel" }] };
        const resp = await gameWriteService.updateGame(gameObj);
        expect(resp.isArchived).toEqual(false)
    })

})