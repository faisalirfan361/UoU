const {Optional} = require('@othree.io/optional')
const {GameConfiguration} = require('../../game-configuration')
/**
 * UserPerformanceService service to expose user performance repo
 * and provide basic utlitlies to it
 * 
 * @param HDMIUserPerformance userPerfomanceRepo 
 * @returns 
 */
function UserPerformanceService(userPerfomanceRepo) {
    return {
        /**
         * get user performance data by userId
         * 
         * @param String userId 
         * @returns 
         */
        get: async (userId) => {
            return userPerfomanceRepo.get(userId)
        },
        /**
         * get user performance by ids list
         * @param array userIds 
         * @returns 
         */
        getBatch: async (userIds) => {
            return userPerfomanceRepo.getBatch(userIds)
        }
    }
}

module.exports = {UserPerformanceService}
