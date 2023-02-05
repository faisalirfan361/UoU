const { Optional } = require("@othree.io/optional");
const { v4: uuid } = require("uuid");
const AWS = require("aws-sdk");
const eventBridge = new AWS.EventBridge();
/**
 * GameWriteService Write service for UOneGames
 * 
 * @param GameWriteRepository gameWriteRepository 
 * @param GameSchedulerService{*} gameSchedulerService 
 * @param EntityReadClient entityReadClient 
 * @param WriteQuestGraphService writeQuestGraphService 
 * @param GameReadRepository gameReadRepository 
 * @param ReadQuestGraphService readQuestGraphService 
 * @returns 
 */
function GameWriteService(
    gameWriteRepository, 
    gameSchedulerService,
    entityReadClient,
    writeQuestGraphService,
    gameReadRepository,
    readQuestGraphService
) {
    return {
        /**
         * insertGame takes gameObject and inserts into dynamodb
         * sets user profiles
         * @param obj game
         * @returns object of game created
         */
        insertGame: async (game) => {
            let teams = game.teams;
            console.log("teams ",game.teams);
            game.gameId = uuid();
            game.createdAt = Date.now();
            game.profiles = [];
            if(teams){
                let userIds = [];
                let deptIds = [];
                let users = [];
                let vals = {
                    VID: teams[0],
                    START: 0,
                    END: 99
                };
                deptIds = await readQuestGraphService.getResultByScript('GROUP_CHILD_GROUPS_ACTIVE', vals);
                console.log("deptIds ", deptIds);
                if(deptIds.length > 0){
                    let ids = deptIds.slice(0, 10);
                    for(let i = 0; i < ids.length; i++){
                        let deptVals = {
                            VID: ids[i],
                            START: 0,
                            END: 99
                        };
                        userIds = userIds.concat(await readQuestGraphService.getResultByScript('GROUP_USERS_ACTIVE', deptVals));
                    }
                }else{
                    userIds = await readQuestGraphService.getResultByScript('GROUP_USERS_ACTIVE', vals);
                }
                let userId = userIds.slice(0, 100);
                const request = {
                    ids: userId,
                }
                let maybeUsers = await entityReadClient.getEntitiesByIdList(request);
                users = maybeUsers?.get();
                console.log(userId);
                console.log(users);

                for (let i = 0; i < users.length; i++) {
                    game.profiles.push({
                        firstName: users[i].attributes.firstName,
                        lastName: users[i].attributes.lastName,
                        departmentId: users[i].groupId,
                        entityId: users[i].entityId,
                        score: 0,
                    });
                }
            }
            for (let i = 0; i < game.challengesUser.length; i++) {
                game.profiles.push({
                    firstName: game.challengesUser[i]["first_name"],
                    lastName: game.challengesUser[i]["last_name"],
                    departmentId: game.challengesUser[i]["team_id"],
                    entityId: game.challengesUser[i]["user_id"],
                    score: 0,
                });
            }
            delete game.challengesUser;
            //unique users in profile by entityId
            game.profiles = [...new Map(game.profiles.map(item => [item['entityId'], item])).values()];
            let response = await gameWriteRepository.upsert(game);
            return response;
        },
        /**
         * Updates or inserts game in quest, it returns nothing as it is an async call on dynamodb stream
         * @param str game
         * @returns null
         */
        insertGameinGraphSvc: async (game) => {
            await writeQuestGraphService.insertGameIntoGraph(
                game,
                writeQuestGraphService
            );
        },
        /**
         * reset Game takes game object and updates in dynamoDb
         * 
         * @TODO Faisal Irfan refactor with Aramis Y. 
         * 
         * @param obj gameObj
         * @returns  object of game updated
         */
        resetGameAndFindWinner: async (gameObj) => {
            console.log("game ", JSON.stringify(gameObj));
            let game = await gameReadRepository.get(gameObj.gameId);
            if (game.isPresent) {
                game = game.get();
            } else {
                console.log("Game not found");
                return;
            }
            if (game.isArchived) {
                await gameSchedulerService.deleteEvent(game.gameId);
                game.isComplete = true;
                await gameWriteRepository.archiveGame(game);
                return
            }

            console.log("game ", JSON.stringify(game));
            let notificationRecipients = [];
            let winnerProfile = game.profiles.reduce(function (prev, current) {
                notificationRecipients.push(current.entityId);
                if (game.kpi && game.kpi.attributes && game.kpi.attributes.flip) {
                    if (prev.score==0)
                        return current
                    if (+current.score != 0 && +current.score < +prev.score) {
                        return current;
                    } else {
                        return prev;
                    }
                }
                else {
                    if (+current.score > +prev.score) {
                        return current;
                    } else {
                        return prev;
                    }
                }
            });
            if (parseInt(winnerProfile.score) != 0) {
                game.winnerProfile = winnerProfile;
                // send message to event bus
                let eventParams = {
                    DetailType: "USER:POINTS_UPDATED",
                    Source: "hdn",
                    EventBusName: "BackboneMainAppBus",
                };
                eventParams["Detail"] = JSON.stringify({
                    pointsToAdd: parseInt(game.winnerPoints),
                    userId: game.winnerProfile.entityId,
                });
                await eventBridge
                    .putEvents({
                        Entries: [eventParams],
                    })
                    .promise();

                eventParams = {
                    DetailType: "USER:CHALLENGE_WON",
                    Source: "hdn",
                    EventBusName: "BackboneMainAppBus",
                };
                eventParams["Detail"] = JSON.stringify({
                    userId: game.winnerProfile.entityId,
                });
                await eventBridge
                    .putEvents({
                        Entries: [eventParams],
                    })
                    .promise();

                // send notifications
                let notifyEventParams = {
                    DetailType: game.isDuel
                        ? "NOTIFICATION:DUEL_WON"
                        : "NOTIFICATION:CHALLENGE_WON",
                    Source: "hdn",
                    EventBusName: "BackboneMainAppBus",
                    Detail: JSON.stringify({
                        sender: game.user_id ? game.user_id : "uone-game",
                        recipients: notificationRecipients,
                        message: `${game.winnerProfile.firstName} ${game.winnerProfile.lastName} has won the '${game.title}' challenge.`,
                        data: {
                            gameId: game.gameId,
                            winner: game.winnerProfile,
                        },
                    }),
                };
                await eventBridge
                    .putEvents({
                        Entries: [notifyEventParams],
                    })
                    .promise();
            }
            else {
                delete game.winnerProfile;
            }
            game.isComplete = true;
            let response = await gameWriteRepository.archiveGame(game);
            await gameSchedulerService.deleteEvent(game.gameId);
            // check ig game is expired
            let isGameExpired = false; 
            let currentDate = new Date();
            let endDate = new Date(game.end_date);

            if (game.schedule == "ONCE" || endDate <= currentDate) return;
            delete game.gameId;
            delete game.isComplete;
            delete game.isArchived; 
            game.gameId = uuid();
            game.createdAt = Date.now();
            for (let i = 0; i < game.profiles.length; i++) {
                game.profiles[i]["score"] = 0;
            }
            response = await gameWriteRepository.upsert(game);
            return game;
        },
        /**
         * updateGame takes game object and updates in dynamoDb
         * @param obj game
         * @returns  object of game updated
         */
        updateGame: async (game) => {
            //archive the game
            let response = await gameWriteRepository.archiveGame(game);
            gameSchedulerService.deleteEvent(game.gameId);
            if (game.isDuel && game.isDeclined) {
                return game
            }
            game.gameId = uuid();
            game.createdAt = Date.now();
            game.profiles = [];
            for (let i = 0; i < game.challengesUser.length; i++) {
                game.profiles.push({
                    firstName: game.challengesUser[i]["first_name"],
                    lastName: game.challengesUser[i]["last_name"],
                    departmentId: game.challengesUser[i]["team_id"],
                    entityId: game.challengesUser[i]["user_id"],
                    score: 0,
                });
            }
            delete game.challengesUser;
            game.isArchived = false;
            game.isComplete = false;
            response = await gameWriteRepository.upsert(game);
            // todo faisal move it where it belongs
            // if (game.isDeclined) {
            //     await gameWriteRepository.update(game);
            //     let notifyEventParams = {
            //         DetailType: "NOTIFICATION:DUEL_ACCEPTED",
            //         Source: "hdn",
            //         EventBusName: "BackboneMainAppBus",
            //         Detail: JSON.stringify({
            //             sender: game.user_id ? game.user_id : "uone-game",
            //             recipients: game.user_id,
            //             message: `Your opponent has declined your DUEL request.`,
            //             data: {
            //                 gameId: game.gameId,
            //             },
            //         }),
            //     };
            //     await eventBridge
            //         .putEvents({
            //             Entries: [notifyEventParams],
            //         })
            //         .promise();
            // }
            return game;
        },
        /**
         * acceptDuel takes duel object and updates in dynamoDb
         * @param str duelId
         * @returns object of game updated
         */
        acceptDuelMSSvc: async (duelId) => {
            try {
                console.log("acceptDuelMSSvc duel Id ", duelId);
                let game = await gameReadRepository.get(duelId);
                if (game.isPresent) game = game.get();
                else return false;
                game.isAccepted = true;

                console.log("get game ", JSON.stringify(game));
                if (game.isAccepted) {
                    await gameWriteRepository.update(game);
                    let notifyEventParams = {
                        DetailType: "NOTIFICATION:DUEL_ACCEPTED",
                        Source: "hdn",
                        EventBusName: "BackboneMainAppBus",
                        Detail: JSON.stringify({
                            sender: "UOne-game",
                            recipients: [game.user_id],
                            message: `Your opponent has accepted your DUEL request.`,
                            data: {
                                gameId: game.gameId,
                            },
                        }),
                    };
                    await eventBridge
                        .putEvents({
                            Entries: [notifyEventParams],
                        })
                        .promise();
                }
            } catch (e) {
                throw e;
            }
            return true;
        },
        /**
         * Notify if game/duel has been declined
         * @param {object} game 
         * @returns 
         */
        notifyDeclinedDuel: async (game) => {
            let notifyEventParams = {
                DetailType: "NOTIFICATION:DUEL_DECLINED",
                Source: "hdn",
                EventBusName: "BackboneMainAppBus",
                Detail: JSON.stringify({
                    sender: game.user_id,
                    recipients: [game.user_id],
                    message: `Your opponent has declined your DUEL request.`,
                    data: {
                        gameId: game.gameId,
                    },
                }),
            };
            await eventBridge
                .putEvents({
                    Entries: [notifyEventParams],
                })
                .promise();
        },
        /**
         * deleteGame by uuid, pass gameId to be deleted
         * @param str gameId
         * @returns object success true or false
         */
        deleteGame: async (gameId) => {
            try {
                let game = await gameReadRepository.get(gameId);
                console.log(game);
                if (game.isPresent) {
                    game = game.get();
                    console.log("get game", game)
                    const response = await gameWriteRepository.archiveGame(game);
                    const result = await writeQuestGraphService.removeGameFromGraph(
                        gameId
                    );
                    console.log("archiveGame", response)
                    console.log("remove from graph", result)
                }
            } catch (ex) {
                console.log(ex);
                return { success: false };
            }

            return { success: true };
        },
        /**
         * deleteGameFromGraph pass gameId to be deleted
         * @param str gameId
         * @returns object success true or false
         */
        deleteGameFromGraph: async (gameId) => {
            try {
                return await writeQuestGraphService.removeGameFromGraph(
                    gameId
                );
            } catch (ex) {
                console.log(ex);
                return { success: false };
            }

            return { success: true };
        },
        /**
         * insertGameInQuestSvc provide old entity and new entity from dynamodb records
         * if old entity is null it means it is a new entity and it inserts it in graphs db
         * @param gameObj newGameObj
         * @param gameObj oldGameObj (null if its a new insert)
         * @returns
         */
        insertGameInQuestSvc: async (game, oldGame = null) => {
            // This is an insert call
            if (!oldGame) {
                return await writeQuestGraphService.insertGameIntoGraph(
                    game,
                    writeQuestGraphService
                );
            }
        },
        /**
         * insertEdgesIntoGraphSvc inset edges into the graph/qeust/neptune
         * @param arr oldGameObjarray of edges quest edge object
         * @returns
         */
        insertEdgesIntoGraphSvc: async (game, oldGame = null) => {
            return await writeQuestGraphService.insertEdgesIntoGraph(
                game,
                writeQuestGraphService
            );
        },
        /**
         * passs game Id and returns game object
         * @param str gameId
         * @returns obj returns game object if found else null
         */
        get: async (gameId) => {
            return await gameWriteRepository.get(gameId);
        },
    };
}

module.exports = { GameWriteService };
