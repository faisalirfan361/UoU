const { GameConstants } = require("../../shared/game-constants");
const { GameUtils } = require("../../shared/game-utils");
const { Optional } = require("@othree.io/optional");
const { v4: uuid } = require("uuid");
const AWS = require("aws-sdk");
AWS.config.update({ region: process.env.REGION });
const eventBridge = new AWS.EventBridge();
var lambda = new AWS.Lambda();

/**
 * GameSchedulerService provide interface create schedule events to run calculations and find winners
 * for the games, goalds, kpis and metrics
 * 
 * @param GameWriteRepository gameWriteRepository 
 * @param ReadQuestGraphService readQuestGraphService 
 * @param EntityReadClient entityReadClient 
 * @returns 
 */
function GameSchedulerService(gameWriteRepository, readQuestGraphService, entityReadClient) {
    return {
        /**
         * creates end event for game so it be archived in default eventbus
         * 
         * @TODO Faisal & Carlos to plan to refactor this function
         * 
         * @param object game
         * @returns
         */
        createGameSchedule: async (game) => {
            const cloudwatchevents = new AWS.CloudWatchEvents();
            console.log("Testing schedule 1");
            let scheduleExpression = GameUtils.generateGameSchedule(
                game,
                GameConstants
            );
            console.log("Testing schedule 2", game);
            console.log("Testing schedule 3", scheduleExpression);
            if (game.isComplete || scheduleExpression == -1) return;
            // send message to event bus for new post
            const feedDetailType = game.isDuel
                ? "DUEL_CREATED"
                : "CHALLENGE_CREATED";
            const feedEventParams = {
                DetailType: `GAMES:${feedDetailType}`,
                Source: "hdn",
                EventBusName: "BackboneMainAppBus",
            };

            feedEventParams["Detail"] = JSON.stringify({
                clientId: game.clientId,
                type: feedDetailType,
                metaData: game,
            });
            await eventBridge
                .putEvents({
                    Entries: [feedEventParams],
                })
                .promise();

            //add all users to game
            let userIds = [];
            let users = [];
            let respDept = [];
            let resp = [];
            let teams = game.teams;
            if (teams)
                for (let i = 0; i < teams.length; i++) {
                    let deptIds = [];
                    let pageCountDept = 0;
                    do {
                        let vals = {
                            VID: teams[i],
                            START: pageCountDept,
                            END: pageCountDept + 99
                        };
                        respDept = await readQuestGraphService.getResultByScript('GROUP_CHILD_GROUPS_ACTIVE', vals);
                        deptIds = deptIds.concat(respDept);
                        pageCountDept += 100;
                    } while (respDept.length > 99)

                    console.log("deptIds ", deptIds);
                    
                    if (deptIds.length > 0) {
                        for (let j = 0; j < deptIds.length; j++) {
                            let pageCountDeptUserID = 0;
                            do {
                                let deptVals = {
                                    VID: deptIds[j],
                                    START: pageCountDeptUserID,
                                    END: pageCountDeptUserID + 99
                                };
                                resp = await readQuestGraphService.getResultByScript('GROUP_USERS_ACTIVE', deptVals);
                                userIds = userIds.concat(resp);
                                pageCountDeptUserID += 100;
                            } while (resp.length > 99)
                        }
                    } else {
                        let response = [];
                        let pageCountUser = 0;
                        do {
                            let vals = {
                                VID: teams[i],
                                START: pageCountUser,
                                END: pageCountUser + 99
                            };
                            response = await readQuestGraphService.getResultByScript('GROUP_USERS_ACTIVE', vals)
                            userIds = userIds.concat(response);
                            pageCountUser += 100;
                        } while (response.length > 99)
                    }
                }

            for (let i = 0; i < userIds.length; i += 100) {
                let userId = userIds.slice(i, 100 + i);
                const request = {
                    ids: userId,
                }
                let maybeUsers = await entityReadClient.getEntitiesByIdList(request);
                users = users.concat(maybeUsers?.get())
            }
            console.log("users ", users);
            for (let i = 0; i < users.length; i++) {
                game.profiles.push({
                    firstName: users[i].attributes.firstName,
                    lastName: users[i].attributes.lastName,
                    departmentId: users[i].groupId,
                    entityId: users[i].entityId,
                    score: 0,
                });
            }
            //unique users in profile by entityId
            game.profiles = [...new Map(game.profiles.map(item => [item['entityId'], item])).values()];
            console.log("game ", game);
            let response = await gameWriteRepository.update(game);
            console.log("response ", response);
            // send message to event bus
            let notificationRecipients = [];
            let eventParams = {
                DetailType: "USER:INCREMENT_CHALLENGE_COUNT",
                Source: "hdn",
                EventBusName: "BackboneMainAppBus",
            };
            for (let i = 0; i < game.profiles.length; i++) {
                eventParams["Detail"] = JSON.stringify({
                    userId: game.profiles[i].entityId,
                });
                notificationRecipients.push(game.profiles[i].entityId);
                await eventBridge
                    .putEvents({
                        Entries: [eventParams],
                    })
                    .promise();
            }

            
            if (game.isDuel && game.game && game.game.user_id) {
                notificationRecipients= notificationRecipients.filter((reipients) => reipients !== game.game.user_id);
            }
            // send notifications
            let notifyEventParams = {
                DetailType: game.isDuel
                    ? "NOTIFICATION:DUEL_CREATED"
                    : "NOTIFICATION:CHALLENGE_CREATED",
                Source: "hdn",
                EventBusName: "BackboneMainAppBus",
                Detail: JSON.stringify({
                    sender: game.user_id ? game.user_id : "uone-game",
                    recipients: notificationRecipients,
                    message: game.isDuel ? `You have been invited to a new duel “${game.title}”. Please go to Duels to accept.` : `New Challenge '${
                        game.title
                    }' Created.`,
                    data: {
                        gameId: game.gameId,
                    },
                }),
            };
            console.log("sending notifs to :", JSON.stringify(notifyEventParams))
            if (notificationRecipients.length)
                await eventBridge
                    .putEvents({
                        Entries: [notifyEventParams],
                    })
                    .promise();

            console.log("scheduleExpression ", scheduleExpression);
            const params = {
                Name: game.gameId,
                ScheduleExpression: scheduleExpression,
                Tags: [
                    {
                        Key: 'PROJECT', 
                        Value: 'UOneGames' 
                    },
                    {
                        Key: 'FUNCTIONALITY', 
                        Value: 'games' 
                    },
                    {
                        Key: 'OWNER', 
                        Value: 'Eng' 
                    },
                    {
                        Key: 'ENVIRONMENT', 
                        Value: process.env.PRETTY_ENV_NAME
                    },
                    {
                        Key: 'CLIENT', 
                        Value: game.clientId
                    }
                ]
            };
            // Create Game Event
            const createdEvent = await new Promise((resolve, reject) => {
                cloudwatchevents.putRule(params, function (err, data) {
                    if (err) {
                        console.log("Error Event Create ", err);
                        reject(err);
                    } else {
                        resolve(data);
                    }
                });
            });
            //console.log("createdEvent ", createdEvent);
            // Attach Target Lambda
            const paramsTarget = {
                Rule: game.gameId,
                Targets: [
                    {
                        Arn: process.env.GAME_CRON_FUNC_ARN,
                        Id: game.gameId,
                        Input: JSON.stringify({ gameId: game.gameId }),
                    },
                ]
            };
            const targetResponse = await new Promise((resolve, reject) => {
                cloudwatchevents.putTargets(paramsTarget, function (err, data) {
                    if (err) {
                        reject(err);
                    } else resolve(data);
                });
            });

            return targetResponse;
        },
        /**
         * deletes event from event bus for given gameId
         * include deleting event rule
         * @param str gameId
         */
        deleteEvent: async (gameId) => {
            const cloudwatchevents = new AWS.CloudWatchEvents();
            var paramsTarget = {
                Ids: [gameId],
                Rule: gameId,
                Force: true,
            };
            try {
                await new Promise((resolve, reject) => {
                    console.log("removeTargets", paramsTarget);
                    cloudwatchevents.removeTargets(
                        paramsTarget,
                        function (err, data) {
                            if (err) {
                                resolve(err);
                            } else {
                                resolve(data);
                            }
                        }
                    );
                });
            } catch (error) {
                console.log("Delete Rule " + gameId, error);
            }
            var params = {
                Name: gameId,
                Force: true,
            };
            try {
                await new Promise((resolve, reject) => {
                    console.log("deleteRule ", params);
                    cloudwatchevents.deleteRule(params, function (err, data) {
                        if (err) {
                            console.log(err);
                            resolve();
                        } else {
                            console.log(data);
                            resolve();
                        }
                    });
                });
            } catch (error) {
                console.log("Delete Rule " + gameId, error);
            }
        },
    };
}

module.exports = { GameSchedulerService };
