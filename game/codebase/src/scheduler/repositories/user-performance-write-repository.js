const {Try} = require('@othree.io/optional')
const AWS = require('aws-sdk');
/**
 * UserPerformanceWriteRepository this provides write path for the output of
 * game scheduler, you should be able to get data for User Performance
 * 
 * @param AWS.DynamoDB.DocumentClient documentClient 
 * @param GameConfiguration gameConfiguration 
 * @returns 
 */
function UserPerformanceWriteRepository(documentClient, gameConfiguration) {

    return {
        /**
         * Store / Upsert user performance data
         * 
         * @param UOneUserPerformaceObject performanceData 
         * @returns 
         */
        upsert: async (performanceData) => { 
            const response = await documentClient.put({
                TableName: gameConfiguration.UserPerformanceTable,
                Item: performanceData,
                ReturnValues: 'NONE',
                ReturnConsumedCapacity: 'NONE',
                ReturnItemCollectionMetrics: 'NONE'
            }).promise()
            return {
                performanceData: performanceData,
                maybePut: response
            }
        },
        /**
         * Get user performance data by ID
         * 
         * @param String userId 
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
        }
    }
}

module.exports = {UserPerformanceWriteRepository}