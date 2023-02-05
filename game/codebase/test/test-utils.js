const { DocumentClient } = require('aws-sdk/clients/dynamodb');

/**
 * Creates a document client that points to a local running dynamodb table
 * @returns document client pointing to local dynamodb instance
 */
 function generateTestDocumentClient() {
    let dynamodDB = new DocumentClient({
        endpoint: process.env.MOCK_DYNAMODB_ENDPOINT,
        sslEnabled: false,
        region: 'local',
        credentials: {
            accessKeyId: 'fakeMyKeyId',
            secretAccessKey: 'fakeSecretAccessKey'
        }
    })
    return dynamodDB
}

module.exports = { generateTestDocumentClient };