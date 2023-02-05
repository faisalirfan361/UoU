const AWS = require('aws-sdk')
const {GameConfiguration} = require('../game-configuration')
const {GameReadRepository} = require('./repositories/game-read-repository')
const {GameReadService} = require('./services/game-read-service')
const {GameSearchService} = require('../shared/game-search-service')
const {GameReadSnsHandlers} = require('./handlers/read/game-read-sns-handlers')
const {GameReadRestHandlers} = require('./handlers/read/game-read-rest-handlers')
const {GameReadServiceHandlers} = require('./handlers/read/game-read-service-handlers')

const documentClient = new AWS.DynamoDB.DocumentClient()

const gameReadRepository = GameReadRepository(documentClient, GameConfiguration)
const gameSearchService = GameSearchService()
const gameReadService = GameReadService(gameReadRepository, gameSearchService)

const gameReadSnsHandlers = GameReadSnsHandlers(gameSearchService)
const gameReadRestHandlers = GameReadRestHandlers(gameReadService)
const gameReadServiceHandlers = GameReadServiceHandlers(gameReadService)
/**
 * 
 * Load dependencies and then expose specific paths to be implemented by consumers
 * 
 */
module.exports = {
    rest: {
        ...gameReadRestHandlers
    },
    service: {
        ...gameReadServiceHandlers
    },
    sns: {
        ...gameReadSnsHandlers
    }
}
