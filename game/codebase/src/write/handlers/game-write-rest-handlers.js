const {createResponse} = require('@heyday/uone-utils').Rest
/**
 * GameWriteRestHandler: this should be the only Rest Write interface for UOneGameService
 * 
 * Since this is part of UOneGame (a core service) We do not have
 * direct rest/APiGateway interface to it, you must implment either
 * UOneGameClient or UOneGameGateway for open paths, check JIRA and 
 * UOneGameGateway for swagger links and client functions
 * 
 * Dependencies
 * @param GameService gameService 
 * @returns 
 */         
function GameWriteRestHandler(gameService) {
    return {
        /**
         * pass game object it returns nserted game with success or error message
         * @param obj gameObj 
         * @returns 
         */
        insertGameHandler: async (gameObj) => { 
            const resp = await gameService.insertGame(gameObj)
            return  createResponse(200, resp)
        },
        /**
         * Update game object in dynamodb
         * @param obj gameObj 
         * @returns retunrs updated object
         */
        updateGameHandler: async (gameObj) => { 
            const resp = await gameService.updateGame(gameObj)
            return  createResponse(200, resp)
        },
        /**
         * pass gameId inside body and it returns success true or false 
         * inside service it archives the game (does nto delete permanently)
         * @param str body 
         * @returns 
         */
        deleteGameHandler: async (body) => {
            console.log("deleteGamebody", body)
            if (body.gameId)
                body = body.gameId
            const resp = await gameService.deleteGame(body)
            return  createResponse(200, resp)
        },
        /**
         * this is triggered by event bridge
         * game is reset and old game archived after finding winner
         * @param object gameObj 
         */
        scheduleGameDetailsHandler: async (gameObj) => { 
            const resp = await gameService.resetGameAndFindWinner(gameObj)
            console.log("game Object", resp);
        },
        /**
         * acceptDuelMSHandler accept duel
         * @param duelId duel 
         * @returns 
         */
        acceptDuelMSHandler: async (body) => {
            const resp = await gameService.acceptDuelMSSvc(body.duelId);
            if (resp)
                return createResponse(200, "Duel accepted successfully.")
            else
                return createResponse(500, "Error updating duel. Make sure duel ID is correct")
        }
    }
}

module.exports = {GameWriteRestHandler}