const table = 'GameTable'
process.env.GAME_TABLE = table

const { GameReadRepository } = require('../../../../src/read/repositories/game-read-repository')
const {GameConfiguration} = require('../../../../src/game-configuration')
const games_large = require('../data/games_large.json');
const {generateTestDocumentClient} = require('../../../test-utils')
let documentClient = generateTestDocumentClient() 
let gameReadRepository

describe('GameReadRepository', () => {

    let readConfiguration = {
        ReadTable: 'GameTable' 
    }    
    const OLD_ENV = process.env;

    beforeEach(() => {
        jest.resetModules() 
        process.env = { ...OLD_ENV };
    });
    jest.setTimeout(30000)
    
    afterAll(() => {
        process.env = OLD_ENV; 
    });
  
    describe('get', () => {
 
        it('should try to get challenges by clientId and return an empty optional if it fails', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
    
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getGamesByClientID(
                "greenix-client", "", lastKey, 10)
            
            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return !game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
    
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get games by client id with Active filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.ACTIVE;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getGamesByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return !game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toBeUndefined()
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get games by client id with Complete filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.COMPLETE;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getGamesByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return !game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toEqual(true)
                expect(game.isComplete).toBeDefined()
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get games by client id with DRAW filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.DRAW;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getGamesByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return !game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toEqual(true)
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });


        it('should try to get duels by client id with no filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
    
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getDuelsByClientID(
                "greenix-client", "", lastKey, 10)
            
            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
    
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get duels by client id with Active filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.ACTIVE;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getDuelsByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toBeUndefined()
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get duels by client id with Complete filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.COMPLETE;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getDuelsByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toEqual(true)
                expect(game.isComplete).toBeDefined()
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

        it('should try to get duels by client id with DRAW filters', async () => { 
            process.env.SearchByClientWithCreatedAt = "SearchByClientWithCreatedAt";
            gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
            let filter = GameConfiguration.GameFilters.DRAW;
            // Insert a sample data into our local test dynamodb table
            const allData = [...new Set([...games_large])]
            // insert data in dynamo
            for (let i=0; i < allData.length; i++ ) {
                await documentClient.put({TableName: readConfiguration.ReadTable, Item: allData[i]}).promise();
            }
            // testing for modules
            let expectedAllChallengeResp = [], respIds = [];
            const lastKey = undefined;
            // Call the method we are testing, with module
            let challengeResp = await gameReadRepository.getDuelsByClientID(
                "greenix-client", filter, lastKey, 10)

            challengeResp.forEach(game => {
                respIds.push(game.gameId)
            })
            expectedAllChallengeResp = games_large.filter(function(game){
                return game.isDuel && respIds.indexOf(game.gameId) > -1
            });

            challengeResp = challengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            expectedAllChallengeResp = expectedAllChallengeResp.sort((a, b) => a.gameId.localeCompare(b.gameId));
            
            // check all are active
            challengeResp.forEach(game => {
                expect(game.isComplete).toEqual(true)
            })
            // no filters tests
            expect(expectedAllChallengeResp.length).toEqual(challengeResp.length); 
            expect(expectedAllChallengeResp).toEqual(challengeResp);
        });

    })

})
