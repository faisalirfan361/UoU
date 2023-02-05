const {EVENT_TYPES} = require('../../shared/event-types')
const {Optional} = require('@othree.io/optional')
const {GameConfiguration} = require('../../game-configuration')

/**
 * GameReadService uses gameSearchService and GameReadRepository 
 * to comunicate and with dynamoDB, Quest and AWS ES, provides utils to 
 * access data 
 * 
 * @param GameReadRepository gameReadRepository 
 * @param GameSearchService gameSearchService 
 * @returns 
 */
function GameReadService(gameReadRepository, gameSearchService) {
    return {
        /**
         * takes games Id and returns game Object if found else null
         * @param str gameId 
         * @returns obj gameObject
         */
        get: async (gameId) => {
            return gameReadRepository.get(gameId)
        },
        /**
         * Games games by search with pagination
         * @param str term 
         * @param str column 
         * @param str page 
         * @param str size 
         * @returns returns list of games found
         */
        getGamesBySearchSvc: async (term, column, page = 0, size = 25) => {
            gameSearchService.setIndex(GameConfiguration.GameIndex)
            let searchResults = await gameSearchService.searchGamesByColumnAndTerm(term, column, page, size);
            return Optional(searchResults)
        },
        /**
         * Games games by search with pagination against clientId
         * @param str clientId 
         * @returns returns list of games found
         */
        getGamesByClientIdSvc: async (clientId, filter="", lastKey) => {
            return await gameReadRepository.getGamesByClientID(clientId, filter, lastKey)
        },
        /**
         * Games duels by search with pagination against clientId
         * @param str clientId 
         * @returns returns list of games found
         */
        getDuelsByClientIdSvc: async (clientId, filter="", lastKey) => {
            return await gameReadRepository.getDuelsByClientID(clientId, filter, lastKey)
        },
        /**
         * Games & duels by List of Ids
         * @param array gameIds 
         * @returns returns list of games found
         */
        getGamesAndDuelsByIdsSvc: async (gameIds) => {
            console.log("service: ", gameIds)
            let games = await gameReadRepository.getGamesAndDuelsByIds(gameIds)
            return games
        }
    }
}

module.exports = {GameReadService}
