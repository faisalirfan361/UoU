/**
 * GameWriteSQSHandlers: this should be the only SQS Write interface for UOneGameService
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
function GameWriteSQSHandlers(gameService) {
    return {
        /**
         * this lambda is triggered by sqs for edges to insert edges into graph
         * @param event sqs event object 
         * @returns 
         */
        upsertEdgesIntoGraphHandler: async (event) => {
            let records = event.Records
            for (let i = 0; i < records.length; i++) {
                let message = JSON.parse(records[i].body)
                console.log("message.edges ", JSON.stringify(message.edges))
                switch (message.action) {
                    case 'REMOVE':
                        //event.Records[0].dynamodb.OldImage
                        break
                    case 'MODIFY':
                        break
                    case 'INSERT':
                        await gameService.insertEdgesIntoGraphSvc(message.edges)
                        break
                }
            }
        }
    }
}

module.exports = {
    GameWriteSQSHandlers
}