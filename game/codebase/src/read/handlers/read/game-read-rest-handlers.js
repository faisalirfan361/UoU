const {createResponse} = require('@heyday/hdmi-utils').Rest
const {GameConfiguration} = require('../../../game-configuration')

/**
 * This is Read Rest path for HdmiGame
 * Since this is part of HDMIGame (a core service) We do not have
 * direct rest/APiGateway interface to it, you must implment either
 * HDMIGameClient or HDMIGameGateway for open paths, check JIRA and 
 * HDMIGameGateway for swagger links and client functions
 * 
 * @param GameReadService gameReadService 
 * @returns 
 */
function GameReadRestHandlers(gameReadService) {

    return {
        /**
         * Get Game by game Id from path parameter
         * @param obj event 
         * @returns gameObject
         */
        getHandler: async (event) => {
            console.log('Getting a game', event)

            const {gameId} = event.pathParameters

            if (!gameId) {
                return createResponse(400, 'id is required')
            }

            const maybeGame = await gameReadService.get(gameId)

            if (maybeGame.isEmpty) {
                console.error('Failed to get game', gameId, maybeGame.getError())
            }

            return maybeGame
                .map(game => {
                    return createResponse(200, game)
                })
                .orElse(createResponse(404))
        },
        /**
         * get list of games by search could be client ID or userId
         * you need to pass term {str} user Id, column name to be searched and type
         * @param obj event 
         * {
         *     "type": "DUELS|GAMES",
         *     "column": "clientId|userId",
         *     "term": "userId or clientId"
         * }
         * @returns list of objs (games)
         */
        getGamesBySearchHandler: async (event) => {

            let {term} = event
            let {column} = event
            let {type} = event
            let {lastKey} = event;
            let {filter} = event

            if (!term) {
                return createResponse(400, 'term is required')
            }
            // We may add more in future
            switch(column) {
                case GameConfiguration.UserSearchType.CLIENT_ID:
                    column = GameConfiguration.GameIndexIDColumns.CLIENT_ID
                    break
                default:
                    column = GameConfiguration.GameIndexIDColumns.USER_ID
            }
            if(filter){
                filter = GameConfiguration.GameFilters[filter.toUpperCase()] || "";
            }
            console.log(" isDuels ", event)
            let maybeGame = []
            try {
                if (type && type == "DUELS")
                    maybeGame = await gameReadService.getDuelsByClientIdSvc(term, filter, lastKey)
                else
                    maybeGame = await gameReadService.getGamesByClientIdSvc(term, filter, lastKey)
            }
            catch(e) {
                console.log("Error getGamesBySearchHandler MS ", e)
                return createResponse(500, maybeGame)
            }
            return createResponse(200, maybeGame)
        },
        /**
         * get list of games by ids in an array
         * @returns array of str (games)
         */
        getGamesByIdsHandler: async (event) => {
            let {gameIds} = event.query
            let gamesList = []
            console.log("Event ", event)
            let resultList = await gameReadService.getGamesAndDuelsByIdsSvc(gameIds);
            console.log("Event resultList ", resultList.getError())
            if (resultList.isPresent) {
                gamesList = resultList.get();
            }
            return gamesList
        }
    }

}

module.exports = {GameReadRestHandlers}
