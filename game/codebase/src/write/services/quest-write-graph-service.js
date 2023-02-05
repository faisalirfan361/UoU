const {GameConstants} = require('../../shared/game-constants.js');
const AWS = require('aws-sdk')
const sqs = new AWS.SQS()
const {Try} = require('@othree.io/optional')

/**
 * WriteQuestGraphService write games, delete games or update games and their
 * relations / hierachies in neptune for games
 * 
 * @param QuestServiceClient questServiceClient 
 * @returns 
 */
function WriteQuestGraphService(questServiceClient) {
    return {
        /**
         * Inserts game into graph and gameGroup
         * @param obj game complate game object 
         * @param WriteQuestGraphService service gameServiceObjects
         */
        insertGameIntoGraph: async (game, service) => {
            let nodes = [service.generateEntity(game)]
            let edges = []
            let gameGroupNode = service.generateEntity(game , true) 
            // Create game Group
            nodes.push(gameGroupNode)
            await service.insertNodesIntoGraph(nodes);
            edges.push(
                service.generateEdge(
                    gameGroupNode.id,
                    game.gameId,
                    GameConstants.GRAPH_RELATIONS.HAS_GAME,
                    true,
                    {
                        from: gameGroupNode.id,
                        to: game.gameId,
                        type: "game_group_edge", 
                        gameId: game.gameId
                    }
                )
            )
            edges.push(
                service.generateEdge(
                    game.gameId,
                    game.kpi_id,
                    GameConstants.GRAPH_RELATIONS.MEASURED_BY,
                    true,
                    {
                        from: game.gameId,
                        to: game.kpi_id,
                        type: "game_kpi_edge", 
                        gameId: game.gameId
                    }
                )
            )
            game.profiles.forEach(profile => {  
                edges.push(
                    service.generateEdge(
                        profile.entityId,
                        gameGroupNode.id,
                        GameConstants.GRAPH_RELATIONS.IN_COMPETITION_WITH,
                        true,
                        {
                            from: profile.entityId,
                            to: gameGroupNode.id,
                            type: "user_game_group_edge",
                            label: game.title,
                            gameId: game.gameId
                        }
                    )
                )
                edges.push(
                    service.generateEdge(
                        gameGroupNode.id,
                        profile.entityId,
                        GameConstants.GRAPH_RELATIONS.IN_COMPETITION_WITH_GAME_GROUP,
                        true,
                        {
                            from: gameGroupNode.id,
                            to: profile.entityId
                        }
                    )
                )
            });
            let edgesArr = []
            for (let i=0; i < edges.length; i++) {
                edgesArr.push(edges[i])
                if (i == edges.length - 1 || edgesArr.length == 25 || edgesArr.length == edges.length) {
                    await service.putEdgesIntoSQS({
                        "action": "INSERT",
                        "edges": edgesArr
                    }) 
                    edgesArr = []
                }
            }
        },
        /**
         * generateEntity generates node/vertex for quest
         * 
         * @param obj gameObj 
         * @param boolean pass true if this is a group for game
         * @returns 
         */
        generateEntity: (gameObj, isGroup=false) => {
            let gameId = gameObj.gameId;
            if (isGroup) 
                return {
                    id: gameId + "_game-group",
                    label: gameObj.title,
                    type: "game_group",
                    isNew: true,
                    payload: {
                        type: "game_group",
                        label: gameObj.title + " group",
                        id: gameId + "_game-group"
                    }
                }
            return {
                    id: gameId,
                    label: gameObj.title,
                    type: "game",
                    payload: {
                        type: "game",
                        label: gameObj.title,
                        id: gameId
                    }
                }
        },
        /**
         * generates edges for games in quest
         * 
         * @param str fromEntityId 
         * @param str toEntityId 
         * @param str label 
         * @param boolean isNew 
         * @param object payload 
         * @returns 
         */
        generateEdge: (fromEntityId , toEntityId, label, isNew=false, payload={}) => {
            let edgeId = fromEntityId  + "-" + toEntityId
            if (isNew) {
                return {
                    "id": edgeId,  
                    "fromId": fromEntityId, 
                    "toId": toEntityId,
                    "label": label,
                    "payload": payload,
                    "isConfirmNew": true
                }
            }
            return {
                "id": edgeId,  
                "fromId": fromEntityId, 
                "toId": toEntityId,
                "label": label,
                "payload": payload
            }
        },
        /**
         * insert nodes in quest 
         * @param array entities 
         * @returns 
         */
        insertNodesIntoGraph: async (entities) => {
            if (!entities || !entities.length) {
                return
            }
            let response = await questServiceClient.upsertEntities(entities);
            if (response.isEmpty) {
                return false
            }
            return true
        },
        /**
         * remove node from quest 
         * @param string gameId 
         * @returns 
         */
        removeGameFromGraph: async (gameId) => {
            console.log("Deleting Game ", gameId)
            let response = await questServiceClient.deleteEntity(gameId);
            console.log("Response ", response)
            if (response.isEmpty) {
                return false
            }
            return true
        },
        /**
         * insert edges in quest
         * @param array edges 
         * @returns 
         */
        insertEdgesIntoGraph: async (edges) => {
            let response = await questServiceClient.upsertPaths(edges)
            if (response.isEmpty) {
                return false
            }
            return true
        },
        /**
         * Makes call to Quest client to store data in DB
         * @param obj entities 
         * @returns 
         */
         insertNodesIntoGraph: async (entities) => {
            if (!entities || !entities.length) {
                return
            }
            let response = await questServiceClient.upsertEntities(entities);
            console.log("response ", response.get())
            if (response.isEmpty) {
                return false
            }
            return true
        },
        /**
         * places Edges in sqs to put a delay in edges creation and node creation
         * @param obj edges 
         * @returns 
         */
        putEdgesIntoSQS: async (msg) => {
            const maybeResult = await Try(async () => {
                return await sqs.sendMessage({
                    MessageBody: JSON.stringify(msg),
                    QueueUrl: process.env.EDGES_QUEUE_URL,
                    DelaySeconds: 45
                }).promise()
            })
            console.log('Message sent ', maybeResult.orElse(maybeResult.getError()))
            return maybeResult.map(_ => msg)
        },
        /**
         * get node by id
         * @param str entityId 
         * @returns 
         */
        getGraphByNodeId: async (entityId) => {
            let node = await questServiceClient.executeContextQuery( entityId, 'Hierarchy');
            return node;
        }
    }
}

module.exports = {WriteQuestGraphService}
