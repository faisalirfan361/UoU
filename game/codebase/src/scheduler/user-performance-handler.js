const {createResponse} = require('@heyday/uone-utils').Rest
const {GameConfiguration} = require('../game-configuration')
const AWS = require('aws-sdk')
/**
 * UserPerformanceHandler exposes function/utlities for GameGateway and GameClient to 
 * get or update user performance data
 * 
 * @param UserPerformanceService userPerformanceService 
 * @returns 
 */
function UserPerformanceHandler(userPerformanceService) {
    return {
        /**
         * request data for userPerformance must pass in userId 
         * to get the performance data for a specific user
         * 
         * @param AWS.Lambda.Event event 
         * @returns 
         */
        getUserPerformanceDataByuserId: async (event) => {
            console.log("getUserPerformanceDataByuserId ", event)
            let {userId} = event
            let {userList} = event
            if (!userId && !userList)
                return createResponse(400, "Must provide userId or userList")
            let userPerformData = {}
            if (userId)
                userPerformData = await userPerformanceService.get(userId)
            if (userList){
                userPerformData = await userPerformanceService.getBatch(userList)
                if (userPerformData.isEmpty) {
                    console.error('Failed to get user Performance data', userId, userPerformData.getError())
                    return createResponse(404, "Data not found for provided user id(s).")
                }
                return createResponse(200, userPerformData)
            }

            if (userPerformData.isEmpty) {
                console.error('Failed to get user Performance data', userId, userPerformData.getError())
            }

            return userPerformData
                .map(data => {
                    return createResponse(200, data)
                })
                .orElse(createResponse(404, "Data not found for provided user id(s)."))
        }
    }
}

module.exports = {UserPerformanceHandler}