const AWS = require('aws-sdk')
const {v4: uuid} = require('uuid')
const {WriteConfiguration} = require('./write-configuration')

const documentClient = new AWS.DynamoDB.DocumentClient()
 
const gameHandler = GameHandler(gameAggregate)
/**
 * Entry point for HDMIGameWrite 
 */
module.exports = {
    ...gameHandler
}
