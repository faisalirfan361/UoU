module.exports = {
    tables: [
        {
            TableName: `GameTable`,
            KeySchema: [{AttributeName: 'gameId', KeyType: 'HASH'}],
            AttributeDefinitions: [
                {
                    AttributeName: 'gameId',
                    AttributeType: 'S'
                }, {
                    AttributeName: 'createdAt',
                    AttributeType: 'N'
                }
                , {
                    AttributeName: 'clientId',
                    AttributeType: 'S'
                }
                //, {
                //     AttributeName: 'cognitoIdentityId',
                //     AttributeType: 'S'
                // }, {
                //     AttributeName: 'type',
                //     AttributeType: 'S'
                // }, {
                //     AttributeName: 'points',
                //     AttributeType: 'N'
                // }
            ],
            ProvisionedThroughput: {ReadCapacityUnits: 10, WriteCapacityUnits: 10},
            GlobalSecondaryIndexes: [
            {
                IndexName: 'SearchByClientWithCreatedAt',
                ProvisionedThroughput: {ReadCapacityUnits: 10, WriteCapacityUnits: 10},
                KeySchema: [{AttributeName: 'clientId', KeyType: 'HASH'}, {AttributeName: 'createdAt', KeyType: 'RANGE'}],
                Projection: {
                    ProjectionType: "ALL"
                }
            }]
        }
    ],
    basePort: 8000
};
