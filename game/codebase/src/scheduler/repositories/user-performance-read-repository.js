const {Try} = require('@othree.io/optional')
const AWS = require('aws-sdk');
/**
 * UserPerformanceReadRepository  this provides read path for the output of
 * game scheduler, you should be able to get data for User Performance
 * 
 * @param AWS.DynamoDB.DocumentClient documentClient 
 * @param GameConfiguration gameConfiguration 
 * @returns 
 */
function UserPerformanceReadRepository(documentClient, gameConfiguration) {

    return {
        /**
         * get: returns performance data for given users
         * 
         * @param {*} userId 
         * @returns 
         */
        get: async (userId) => {
            return Try(async () => {
                const result = await documentClient.get({
                    TableName: gameConfiguration.UserPerformanceTable,
                    Key: {
                        userId: userId
                    }
                }).promise()

                return result.Item
            })
        },
        /**
         * Array of Users for which you need data
         * @param array userIds
         * @returns 
         */
        getBatch: async (userIds, lastKey = undefined) => {
            let performanceData = [], resp
            resp = await Try(async () => {
                let queryParams = {RequestItems: {}};
                let userKeys = []
                for (let i=0; i < userIds.length; i++) {
                    userKeys.push({
                        'userId': userIds[i]
                    })
                }
                queryParams.RequestItems[gameConfiguration.UserPerformanceTable] = {
                    Keys: userKeys
                };
                let result = await documentClient.batchGet(queryParams).promise();
                if (result.Responses && result.Responses.hasOwnProperty(gameConfiguration.UserPerformanceTable)) {
                    result = result.Responses[gameConfiguration.UserPerformanceTable]
                }
                
                console.log("Result ", JSON.stringify(result))
                return result
            })
            performanceData = resp.get()
            return performanceData
        }
    }
}

module.exports = {UserPerformanceReadRepository}