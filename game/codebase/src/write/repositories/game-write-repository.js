const {Try} = require('@othree.io/optional')
const AWS = require('aws-sdk');
/**
 * GameWriteRepository the only write path to games dynamodb
 * HDMI-Game-ENV (dyanoDB)
 * 
 * @param AWS.DynamoDB.DocumentClient documentClient 
 * @param DameConfiguration gameConfiguration 
 * @returns 
 */
function GameWriteRepository(documentClient, gameConfiguration) {

    return {
        /**
         * Upserts game object in dynamoDB
         * 
         * @param HDMIGame game 
         * @returns 
         */
        upsert: async (game) => { 
            const maybePut = await Try(async () => {
                return documentClient.put({
                    TableName: gameConfiguration.GameTable,
                    Item: game,
                    ReturnValues: 'NONE',
                    ReturnConsumedCapacity: 'NONE',
                    ReturnItemCollectionMetrics: 'NONE'
                }).promise()
            })
            return {
                game: game,
                maybePut: maybePut
            }
        },
        /**
         * Since with never delete a game we just archive it using this function
         * 
         * @param HDMIGame game 
         * @returns 
         */
        archiveGame: async (game) => {
            game.isArchived = true
            let resp = await Try(async () => {
                return await documentClient.put({
                    TableName: gameConfiguration.GameTable,
                    Item: game,
                    ReturnValues: 'NONE',
                    ReturnConsumedCapacity: 'NONE',
                    ReturnItemCollectionMetrics: 'NONE'
                }).promise()
            })
            return resp
        },
        /**
         * update games in dynamo
         * it does remove schedules which are then added back by the triggers
         * 
         * @param HDMIGame game 
         */
        update: async (game) => {
            let expressionAttributeNames = Object.entries(game).reduce((acc, cur) => ({...acc, [`#${cur[0]}`]: cur[0]} ), {})
            let expressionAttributeValues =  Object.entries(game).reduce((acc, cur) => ({...acc, [`:${cur[0]}`]: cur[1]}), {})
            let experession =  Object.keys(game).map(k => { if(k!=="gameId") return `#${k} = :${k}` });
            experession = experession.filter(function(x) { return x !== undefined; });
            experession = experession.join(', ') ;
            delete expressionAttributeValues[':gameId'];
            delete expressionAttributeNames['#gameId'];
            let resp = await documentClient.update({
                TableName: gameConfiguration.GameTable,
                Key: {
                    gameId: game.gameId
                },
                UpdateExpression: 'set ' + experession,
                ExpressionAttributeNames: expressionAttributeNames,
                ExpressionAttributeValues: expressionAttributeValues,
            }).promise();
            console.log("Response ", resp)
        },
        /**
         * We only delete the game when Admin request is 
         * HDMIAdmin Only
         * 
         * @param String gameId 
         * @returns 
         */
        delete: async (gameId) => {
            const maybeDelete = await Try(async () => {
                return documentClient.delete({
                    TableName: gameConfiguration.GameTable,
                    Key: {
                        gameId: gameId
                    },
                    ReturnValues: 'NONE',
                    ReturnConsumedCapacity: 'NONE',
                    ReturnItemCollectionMetrics: 'NONE'
                }).promise()
            })
            return maybeDelete.map(_ => gameId)
        },
        /**
         * Get Game By ID
         * 
         * @param String gameId 
         * @returns 
         */
        get: async (gameId) => {
            return await Try(async () => {
                const result = await documentClient.get( {
                    "TableName": gameConfiguration.GameTable,
                    "ReturnConsumedCapacity": "TOTAL",
                    "Limit": 1,
                    "KeyConditionExpression": "#kn0 = :kv0",
                    "ExpressionAttributeNames": {
                        "#kn0": "gameId"
                    },
                    "ExpressionAttributeValues": {
                        ":kv0": gameId
                    }
                }).promise()
                console.log("get game ", JSON.stringify(result))
                return result.Item
            })
        }
    }
}

module.exports = {GameWriteRepository}
