const AWS = require('aws-sdk')
const { GameConfiguration } = require('../../../src/game-configuration')
const { GameReadRepository } = require('../../../src/read/repositories/game-read-repository')
const { ReadQuestGraphService } = require('../../../src/read/services/game-read-graph-service')
const { GameSearchService } = require('../../../src/shared/game-search-service')
const { GameWriteRepository } = require('../../../src/write/repositories/game-write-repository')
const { GameSchedulerService } = require('../../../src/write/services/game-schedule-service')
const { GameWriteService } = require('../../../src/write/services/game-write-service')
const { WriteQuestGraphService } = require('../../../src/write/services/quest-write-graph-service')
const { LambdaInvoker } = require('@heyday/hdmi-utils').Lambda
const { EntityReadClients } = require('@hdmi/hdmi-entity-clients').Read
const { QuestServiceClient } = require('@hdmi/quest-clients').Service;
const games_large = require('../data/game_insert.json');
const testUsers = require('../data/client-multiple-groups.json');
const groups = require('../data/groups.json');

describe('GameWriteService', () => {
    const lambda = new AWS.Lambda()
    const lambdaInvoker = LambdaInvoker(lambda)
    const entityReadClient = EntityReadClients(lambdaInvoker, GameConfiguration)
    const documentClient = new AWS.DynamoDB.DocumentClient()
    const clientConfiguration = {
        UpsertEntitiesFunction: process.env.UPSERT_ENTITIES_FN,
        UpsertPathsFunction: process.env.UPSERT_PATH_FN,
        ExecuteContextQueryFunction: process.env.EXECUTE_QUERY_FN,
        DeleteEntityFunction: process.env.DELETE_QUEST_ENTITY
    };

    const gameReadRepository = GameReadRepository(documentClient, GameConfiguration)

    const questServiceClient = QuestServiceClient(lambdaInvoker, clientConfiguration);
    const writeQuestGraphService = WriteQuestGraphService(questServiceClient)
    const readQuestGraphService = ReadQuestGraphService(questServiceClient)
    readQuestGraphService.getResultByScript = async function (script, vals) {
        let resp = []
        switch(script) {
            case "GROUP_CHILD_GROUPS_ACTIVE":
                for (let i = 0; i < groups.length; i++) {
                    if (groups[i].isActive && groups[i].groupId == vals.VID)
                        resp.push(groups[i].entityId)
                }
                break
            case "GROUP_USERS_ACTIVE":
                for (let i = 0; i < testUsers.length; i++) {
                    if (testUsers[i].isActive && testUsers[i].groupId == vals.VID)
                        resp.push(testUsers[i].entityId)
                }
                break
        }
        console.log(script, resp);
        return resp
    }
    entityReadClient.getEntitiesByIdList = async function (request) {
        let user = [];
        user = testUsers.filter(id => request.ids.find(uid => uid == id.entityId));
        return user;
    }
    const gameSearchService = GameSearchService()
    const gameSchedulerService = GameSchedulerService(readQuestGraphService, entityReadClient)
    const gameWriteRepository = GameWriteRepository(documentClient, GameConfiguration)
    const gameWriteService = GameWriteService(gameWriteRepository, gameSearchService, GameConfiguration, gameSchedulerService, entityReadClient, writeQuestGraphService, gameReadRepository, readQuestGraphService)

    it('should insert Game and add users', async () => {
        const gameData = games_large[0]
        let game = await gameWriteService.insertGame(gameData);
        expect(game.game).toBeDefined();
    })

    it('should not include duplicate entries of users', async () => {
        const gameData = games_large[1]
        let game = await gameWriteService.insertGame(gameData);
        expect(game.game.profiles.length).toEqual(8);
    })

    it('should insert Game and add users if teams not selected', async () => {
        const gameData = games_large[2]
        let game = await gameWriteService.insertGame(gameData);
        expect(game.game.profiles.length).toEqual(3);
    })

    it('should insert Game and add users if users not selected', async () => {
        const gameData = games_large[3]
        let game = await gameWriteService.insertGame(gameData);
        expect(game.game.profiles.length).toEqual(4);
    })
})
