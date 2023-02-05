const { HdmiTagHandler } = require("./hdmi-tag-handler")
const cdk = require('aws-cdk-lib');
const { TAG_CONSTANTS } = require("./utils");
/**
 * This works as wrapper arround all the dyanmoDBs
 * it must be used in all your stacks to make sure all dynamodb are property tagged
 * and tracked across multiple accounts, stacks, services and environments
 * 
 * @param HDNCdkContext cdkContext 
 * @param HDMIStackScope scope 
 * @param AWS.DynamoDB dynamodb 
 * @param HDMIStackCore core 
 * @param HDMIStack HdmiStack 
 * @returns 
 */
function HdmiDynamo(cdkContext, scope, dynamodb, core, HdmiStack) {
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
    const hdmiTagHandler = HdmiTagHandler(cdk.Tags, cdkContext)

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
             * Extends HdmiStack please check HdmiStack for more details
             * but it does basic taggings, datadog and hystrix mappings
             */
            class PersistenceStack extends HdmiStack {
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
                tableName: `Hdmi-${projectName}-Events-${envName}`,
                ...props
            }

            const myStack = new PersistenceStack(cdkContext, scope, `Hdmi-${projectName}-Write-Persistence-${envName}`, stackProps, tableConfiguration);
            // functionality and project/service specific tags
            hdmiTagHandler.tag(
                myStack,
                TAG_CONSTANTS.FUNCTIONALITY,
                functionality
            );
            return myStack
        }
    })
}

module.exports = {HdmiDynamo}
