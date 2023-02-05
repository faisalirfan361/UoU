const AWS = require('aws-sdk')   

/**
 * GameWriteDynamoHandler: this should be used to write from AWS DynamoDB Trigger
 * this is the only tunnel for write page using DynamoDB triggers
 * 
 * Since this is part of HDMIGame (a core service) We do not have
 * direct rest/APiGateway interface to it, you must implment either
 * HDMIGameClient or HDMIGameGateway for open paths, check JIRA and 
 * HDMIGameGateway for swagger links and client functions
 * 
 * Dependencies
 * @param GameService gameService 
 * @param GameSchedulerService gameSchedulerService
 * @returns 
 */
function GameWriteDynamoHandler(gameService, gameSchedulerService) {
    return {
        /**
         * upsert Entity in quest 
         * this removes from quest when event.Records[i].eventname is REMOVE
         * this updates quest when event.Records[i].eventname is MODIFY
         * this inserts in quest when event.Records[i].eventname is INSERT
         * it also creates deletes and udpates the edges
         * @param event dynamodb stream event object 
         * @returns 
         */
        upsertGameInGraphHandler: async (event) => {
            console.log("Recods :", JSON.stringify(event.Records))
            let records = event.Records
            for (let i = 0; i < records.length; i++) {
                switch (records[i].eventName) {
                    case 'REMOVE':
                        let gameObj = AWS.DynamoDB.Converter.unmarshall(event.Records[i].dynamodb.OldImage)
                        await gameSchedulerService.deleteEvent(gameObj.gameId) 
                        if (gameObj.isArchived)
                            await gameService.deleteGameFromGraph(gameObj.gameId)
                        break
                    case 'MODIFY':
                        let game = AWS.DynamoDB.Converter.unmarshall(event.Records[i].dynamodb.OldImage)
                        let gameNew = AWS.DynamoDB.Converter.unmarshall(event.Records[i].dynamodb.NewImage)
                        if(game.isArchived || game.isComplete) {
                            await gameSchedulerService.deleteEvent(game.gameId)
                            await gameService.deleteGameFromGraph(game.gameId)
                        }
                        if (gameNew.isDuel && gameNew.isDeclined) {
                            await gameService.notifyDeclinedDuel(gameNew)
                        }
                        break
                    case 'INSERT':
                        let recordNew = AWS.DynamoDB.Converter.unmarshall(event.Records[i].dynamodb.NewImage)
                        await gameService.insertGameInQuestSvc(recordNew)
                        await gameSchedulerService.createGameSchedule(recordNew)
                        break
                }
            }

        }
    }
}

module.exports = {GameWriteDynamoHandler}