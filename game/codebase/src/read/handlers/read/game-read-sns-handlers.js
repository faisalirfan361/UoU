const GAME_STATE_INDEX = "game_state_index";
const {v4: uuid} = require('uuid')
/**
 * This is Read S path for HdmiGame
 * Since this is part of HDMIGame (a core service) We do not have
 * direct rest/APiGateway interface to it, you must implment either
 * HDMIGameClient or HDMIGameGateway for open paths, check JIRA and 
 * HDMIGameGateway for swagger links and client functions
 * 
 * @param GameReadService gameReadService 
 * @returns 
 */
function GameReadSnsHandlers(gameSearchService) {

    return {
        /**
         * to read metrics from sns
         * retired
         * @param obj event 
         *  {   
         *      metric: {
         *          code: "code",
         *          value: "value",
         *          userId: "userId",
         *          id: "uuid"
         *      } 
         * }
         * @returns 
         */
        readMetricCalculateScoreAndIndex: async (event) => {
            event = JSON.parse(event)
            console.log("event", event)
            if (!event.passthrough || !event.value || !event.kpi) {
                return "no input records found";
            }
            gameSearchService.setIndex(GAME_STATE_INDEX);
            
            console.log("readMetricCalculateScoreAndIndex ", gameSearchService.getIndex());
            // Read from ES
            for (var i = 0; i < event.passthrough.games.length; i++) {
                let initialScore = 0;
                let gameState = await gameSearchService.findGameStateByUserIdAndGameId(event.passthrough.games[i], event.passthrough['hdn-id'])
                if (gameState["games"] && gameState["games"].length) {
                   console.log("gameState" , JSON.stringify(gameState))
                }
                let document = {}
                document.kpi = event.kpi
                document.value = event.value
                document.user_id = event.passthrough['hdn-id']
                document.game_id = event.passthrough.games[i]
                document.id = event.passthrough.games[i]
                gameSearchService.indexDocumentSelfFormatedDocument(document, document.id)
            }
            
            return Promise.all(promises)
        }
    }
}

module.exports = {GameReadSnsHandlers}
