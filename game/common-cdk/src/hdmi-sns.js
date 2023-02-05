const environment = require('./environment-configuration')
const { HdmiTagHandler } = require('./hdmi-tag-handler')
const cdk = require('aws-cdk-lib');
const { TAG_CONSTANTS } = require("./utils");

/**
 * this should be used in each stack to provide async write route
 * 
 * @param HDNCdkContext cdkContext 
 * @param AWS.SNS sns 
 * @param HdmiStack HdmiStack 
 * @returns 
 */
function HdmiSns(cdkContext, sns, HdmiStack) {
    if(!cdkContext) {
        throw new Error('cdkContext is required')
    }

    if(!sns) {
        throw new Error('sns is required')
    }

    const {envName, volatile} = cdkContext
    const hdmiTagHandler = HdmiTagHandler(cdk.Tags, cdkContext)

    return Object.freeze({
        /**
         * load existing SNS Topic by name
         * 
         * @param HDMIStackScope scope 
         * @returns 
         */
        fromTopicName: (scope) => (topicName, accountId = environment.getAccount(), region = environment.getRegion()) => {
            if(!scope) {
                throw new Error('scope is required')
            }

            if(!topicName) {
                throw new Error('topicName is required')
            }

            if(!accountId) {
                if (cdkContext.envObj) {
                    accountId = cdkContext.envObj.account;
                    region = cdkContext.envObj.region;
                }
                else
                    throw new Error('accountId is required')
            }

            
            const snsTopic = sns.Topic.fromTopicArn(scope, topicName, `arn:aws:sns:${region}:${accountId}:${topicName}`)

            return snsTopic;
        },
        /**
         * generate new async route for your stack, you mush provide listener along with the props
         * 
         * @param HDMIStackScope scope 
         * @param String projectName 
         * @param String functionality 
         * @param HDMIProps props 
         * @returns 
         */
        newDefaultEventBusStack: function(scope, projectName, functionality, props){
            if(!scope) {
                throw new Error('scope is required')
            }

            if(!projectName) {
                throw new Error('projectName is required')
            }

            if(!functionality) {
                throw new Error('functionality is required')
            }

            class EventBusStack extends HdmiStack {
                constructor(cdkContext, scope, id, props) {
                    super(cdkContext, scope, id, props)
                    this.topicName = props.topicName

                    new sns.Topic(this, props.topicName, {
                        topicName: props.topicName,
                        displayName: `${props.projectName} Event Bus`
                    })
                }
            }

            const stackProps = {
                topicName: `Hdmi-${projectName}-Topic-${envName}`,
                projectName: projectName,
                ...props
            }

            const eventBusStack = new EventBusStack(cdkContext, scope, `Hdmi-${projectName}-EventBus-${envName}`, stackProps);
            hdmiTagHandler.tag(
                eventBusStack,
                TAG_CONSTANTS.FUNCTIONALITY,
                functionality
            );
            return eventBusStack;
        }
    })
}

module.exports = {HdmiSns}
