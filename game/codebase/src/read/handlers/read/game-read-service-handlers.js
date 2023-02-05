/**
 * This is Read SNS path for UOneGame
 * 
 * This route is only used for async requests to the UOneGame Service
 * We are currently using is for calulating games, goals and kpi scores.
 * 
 * Since this is part of UOneGame (a core service) We do not have
 * direct rest/APiGateway interface to it, you must implment either
 * UOneGameClient or UOneGameGateway for open paths, check JIRA and 
 * UOneGameGateway for swagger links and client functions
 * 
 * @param GameReadService gameReadService 
 * @returns 
 */
function GameReadServiceHandlers(gameReadService) {
    return {
        /**
         * this is a service or lambda function to return game by Id
         * to be used internally by other micros
         * @param obj event { gameId : "uuid"}
         * @returns 
         */
        getHandler: async (event) => {
            console.log('Getting single game', event)
            const {gameId} = event.query
            const maybeGame = await gameReadService.get(gameId)

            if (maybeGame.isEmpty) {
                console.error(`Failed get game: ${gameId}`, maybeGame.getError())
                throw maybeGame.getError()
            }

            return maybeGame.get()
        }
    }
}

module.exports = {GameReadServiceHandlers}
