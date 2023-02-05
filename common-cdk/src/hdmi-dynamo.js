const { UOneTagHandler } = require("./uone-tag-handler")
const cdk = require('aws-cdk-lib');
const { TAG_CONSTANTS } = require("./utils");
/**
 * This works as wrapper arround all the dyanmoDBs
 * it must be used in all your stacks to make sure all dynamodb are property tagged
 * and tracked across multiple accounts, stacks, services and environments
 * 
 * @param HDNCdkContext cdkContext 
 * @param UOneStackScope scope 
 * @param AWS.DynamoDB dynamodb 
 * @param UOneStackCore core 
 * @param UOneStack UOneStack 
 * @returns 
 */
function UOneDynamo(cdkContext, scope, dynamodb, core, UOneStack) {
    if(!cdkContext) {
        throw new Error('cdkContext is required')
    }

    if(!scope) {
        throw new Error('scope is required')
    }

    if(!dynamodb) {
        throw new Error('dynamodb is required')
    }

    const {envName, volatile} = cdkContext
    const uoneTagHandler = UOneTagHandler(cdk.Tags, cdkContext)

    return Object.freeze({
        newDefaultPersistenceStack: function (projectName, functionality, tableConfiguration = {}, props = {}) {
            if (!projectName) {
                throw new Error('projectName is required')
            }

            if(!functionality) {
                throw new Error('functionality is required')
            }

            const removalPolicy = volatile ? core.RemovalPolicy.DESTROY : core.RemovalPolicy.RETAIN

            /**
             * Extends UOneStack please check UOneStack for more details
             * but it does basic taggings, datadog and hystrix mappings
             */
            class PersistenceStack extends UOneStack {
                constructor(cdkContext, scope, id, props, tableConfiguration) {
                    super(cdkContext, scope, id, props)
                    this.tableName = props.tableName
                    new dynamodb.Table(this, props.tableName, {
                        tableName: props.tableName,
                        partitionKey: {name: 'contextId', type: dynamodb.AttributeType.STRING},
                        sortKey: {name: 'eventDate', type: dynamodb.AttributeType.NUMBER},
                        billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
                        removalPolicy: removalPolicy,
                        ...tableConfiguration
                    })
                }
            }

            const stackProps = {
                tableName: `UOne-${projectName}-Events-${envName}`,
                ...props
            }

            const myStack = new PersistenceStack(cdkContext, scope, `UOne-${projectName}-Write-Persistence-${envName}`, stackProps, tableConfiguration);
            // functionality and project/service specific tags
            uoneTagHandler.tag(
                myStack,
                TAG_CONSTANTS.FUNCTIONALITY,
                functionality
            );
            return myStack
        }
    })
}

module.exports = {UOneDynamo}
