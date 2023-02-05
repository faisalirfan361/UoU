const { Try } = require("@othree.io/optional");
const { GameConfiguration } = require("../../game-configuration");
/**
 * GameReadRepository is dynamo Repo for all the games
 * HDMI-Games-Env should gives you access in AWS Console.
 * 
 * @param AWS.DynamoDB.DocumentClient documentClient 
 * @param ReadConfiguration readConfiguration 
 * @returns 
 */
function GameReadRepository(documentClient, readConfiguration) {
    return {
        /**
         * insert or update games in dynamodb
         * @param obj game gameObject
         * @returns gameObject
         */
        upsert: async (game) => {
            const maybePut = await Try(async () => {
                return documentClient
                    .put({
                        TableName: readConfiguration.ReadTable,
                        Item: game,
                        ReturnValues: "NONE",
                        ReturnConsumedCapacity: "NONE",
                        ReturnItemCollectionMetrics: "NONE",
                    })
                    .promise();
            });
            return maybePut.map((_) => game);
        },
        /**
         * deletes games by given gameId
         * @param str gameId
         * @returns str deleted gameId
         */
        delete: async (gameId) => {
            const maybeDelete = await Try(async () => {
                return documentClient
                    .delete({
                        TableName: readConfiguration.ReadTable,
                        Key: {
                            gameId: gameId,
                        },
                        ReturnValues: "NONE",
                        ReturnConsumedCapacity: "NONE",
                        ReturnItemCollectionMetrics: "NONE",
                    })
                    .promise();
            });
            return maybeDelete.map((_) => gameId);
        },
        /**
         * ateks gameid and returns game against the id
         * @param str gameId
         * @returns obj gameObjects
         */
        get: async (gameId) => {
            return await Try(async () => {
                console.log({
                    TableName: process.env.GAME_TABLE,
                    Key: {
                        gameId: gameId,
                    },
                });
                const result = await documentClient
                    .get({
                        TableName: process.env.GAME_TABLE,
                        Key: {
                            gameId: gameId,
                        },
                    })
                    .promise();
                return result.Item;
            });
        },
        /**
         * takes client Id and returns all games against the client
         * @param str clientId
         * @returns array gamesObj
         */
        getGamesByClientID: async (
            clientId,
            filter = "",
            lastKey = undefined,
            limit = 10
        ) => {
            try {
                let games = [],
                    resp;
                let exclusiveStartKey = lastKey;
                let exclusiveStartDate = 0;
                let LastEvaluatedKey = null;
                
                if (lastKey) {
                    const result = await documentClient
                    .get({
                        TableName: process.env.GAME_TABLE,
                        Key: {
                            gameId: lastKey,
                        },
                    })
                    .promise();
                    if (result.Item) {
                        exclusiveStartDate = result.Item.createdAt;
                    }
                }

                do {
                    resp = await Try(async () => {
                        let params = {
                            TableName: GameConfiguration.GameTable,
                            ReturnConsumedCapacity: "TOTAL",
                            Limit: 1000,
                            KeyConditionExpression: "#kn0 = :kv0",
                            IndexName:
                                GameConfiguration.GameIndexes
                                    .SEARCH_BY_CLIENT_ID_WITH_CREATED_AT,
                            ScanIndexForward: false,
                            FilterExpression:
                                "#n0 <> :true" +
                                (filter ? " and " + filter : ""),
                            ExpressionAttributeNames: {
                                "#n0": "isDuel",
                                "#kn0": "clientId",
                            },
                            ExpressionAttributeValues: {
                                ":true": true,
                                ":kv0": clientId,
                            },
                        };
                        if (exclusiveStartKey) {
                            params["ExclusiveStartKey"] = {
                                clientId: clientId,
                                gameId: exclusiveStartKey,
                                createdAt: exclusiveStartDate,
                            };
                            exclusiveStartKey = undefined;
                        }
                        if (games.length < limit && games[games.length - 1]) {
                            params["ExclusiveStartKey"] = {
                                clientId: clientId,
                                gameId: games[games.length - 1].gameId,
                                createdAt: games[games.length - 1].createdAt,
                            };
                        }
                        const result = await documentClient
                            .query(params)
                            .promise();
                        LastEvaluatedKey = result.LastEvaluatedKey;
                        return result.Items;
                    });
                    games = games.concat(resp.get());
                } while (games.length < limit && LastEvaluatedKey);
                if (games.length > limit) {
                    games = games.slice(0, limit);
                }
                return games;
            } catch (e) {
                console.log(e);
            }
        },
        /**
         * get Duels by client Id
         * @param str clientId
         * @returns array gamesObj
         */
        getDuelsByClientID: async (
            clientId,
            filter = "",
            lastKey = undefined,
            limit = 10
        ) => {
            try {
                let games = [],
                    resp;
                let exclusiveStartKey = lastKey;
                let exclusiveStartDate = 0;
                let LastEvaluatedKey = null;

                if (lastKey) {
                    const result = await documentClient
                    .get({
                        TableName: process.env.GAME_TABLE,
                        Key: {
                            gameId: lastKey,
                        },
                    })
                    .promise();
                    if (result.Item) {
                        exclusiveStartDate = result.Item.createdAt;
                     }
                 }
  
                do {
                    resp = await Try(async () => {
                        let params = {
                            TableName: GameConfiguration.GameTable,
                            ReturnConsumedCapacity: "TOTAL",
                            Limit: 2000,
                            KeyConditionExpression: "#kn0 = :kv0",
                            IndexName:
                                GameConfiguration.GameIndexes
                                    .SEARCH_BY_CLIENT_ID_WITH_CREATED_AT,
                            ScanIndexForward: false,
                            FilterExpression:
                                "#n0 = :true" +
                                (filter ? " and " + filter : ""),
                            ExpressionAttributeNames: {
                                "#n0": "isDuel",
                                "#kn0": "clientId",
                            },
                            ExpressionAttributeValues: {
                                ":true": true,
                                ":kv0": clientId,
                            },
                        };
                        if (exclusiveStartKey) {
                            params["ExclusiveStartKey"] = {
                                clientId: clientId,
                                gameId: exclusiveStartKey,
                                createdAt: exclusiveStartDate,
                            };
                            exclusiveStartKey = undefined;
                        }
                        if (games.length < limit && games[games.length - 1]) {
                            params["ExclusiveStartKey"] = {
                                clientId: clientId,
                                gameId: games[games.length - 1].gameId,
                                createdAt: games[games.length - 1].createdAt,
                            };
                        }
                        const result = await documentClient
                            .query(params)
                            .promise();
                            
                        LastEvaluatedKey = result.LastEvaluatedKey;
                            
                        return result.Items;
                    });
                    games = games.concat(resp.get());
                    console.log("Game resp ", JSON.stringify(games)) 
                } while (games.length < limit && LastEvaluatedKey);
                if (games.length > limit) {
                    games = games.slice(0, limit);
                }
                return games;
            } catch (e) {
                console.log(e);
            }
	    },
        /**
         * get Duels by client Id
         * @param array gameIds
         * @returns array gamesObj
         */
        getGamesAndDuelsByIds: async (gameIds) => {
            console.log("game IDs :", gameIds);
            return Try(async () => {
                let queryParams = { RequestItems: {} };
                let gameKeys = [];
                for (let i = 0; i < gameIds.length; i++) {
                    gameKeys.push({
                        gameId: gameIds[i],
                    });
                }
                queryParams.RequestItems[process.env.GAME_TABLE] = {
                    Keys: gameKeys,
                };
                let result = await documentClient
                    .batchGet(queryParams)
                    .promise();
                if (
                    result.Responses &&
                    result.Responses.hasOwnProperty(process.env.GAME_TABLE)
                ) {
                    result = result.Responses[process.env.GAME_TABLE];
                }

                console.log("Result ", JSON.stringify(result));
                return result;
            });
        },
        /**
         * returns list of games invloves a department
         * @param str departmentId
         * @returns array gamesObj
         */
        getByDepartment: async (departmentId) => {
            return Try(async () => {
                const result = await documentClient
                    .get({
                        TableName: readConfiguration.ReadTable,
                        Key: {
                            id: departmentId,
                        },
                    })
                    .promise();

                return result.Item;
            });
        },
        /**
         * Get Game by Id
         * @param str gameId
         * @returns
         */
        getGameById: async (gameId) => {
            return await Try(async () => {
                try {
                    const result = await documentClient
                        .query({
                            TableName: process.env.GAME_TABLE,
                            ReturnConsumedCapacity: "TOTAL",
                            Limit: 1,
                            KeyConditionExpression: "#kn0 = :kv0",
                            ExpressionAttributeNames: {
                                "#kn0": "gameId",
                            },
                            ExpressionAttributeValues: {
                                ":kv0": gameId,
                            },
                        })
                        .promise();
                    console.log("resultresultresult ", JSON.stringify(result));
                    return result.Items;
                } catch (e) {
                    console.log("Error getGameById: error loading game  ", e);
                }
            });
        },
    };
}

module.exports = { GameReadRepository };
