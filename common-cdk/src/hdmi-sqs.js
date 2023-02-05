const environment = require('./environment-configuration')
const { UOneTagHandler } = require('./uone-tag-handler')
const cdk = require('aws-cdk-lib');
const { TAG_CONSTANTS } = require("./utils");

/**
 * this should be used in each stack to provide async write route
 * 
 * @param HDNCdkContext cdkContext 
 * @param UOneStackScope scope 
 * @param AWS.SQS sqs 
 * @param UOneStackCore core 
 * @param UOneStack UOneStack 
 * @returns 
 */
function UOneSqs(cdkContext, scope, sqs, core, UOneStack) {
    if(!cdkContext) {
        throw new Error('cdkContext is required')
    }

    if(!scope) {
        throw new Error('scope is required')
    }

    if(!sqs) {
        throw new Error('sqs is required')
    }

    const {envName, volatile} = cdkContext

    return Object.freeze({
        /**
         * load existing SQS by name
         * 
         * @param String queueName 
         * @returns 
         */
        fromQueueName: (queueName, accountId = environment.getAccount(), region = environment.getRegion()) => {
            if(!queueName) {
                throw new Error('queueName is required')
            }

            if(!accountId) {
                if (cdkContext.envObj) {
                    accountId = cdkContext.envObj.account;
                    region = cdkContext.envObj.region;
                }
                else
                    throw new Error('accountId is required')
            }

            return sqs.Queue.fromQueueArn(scope, queueName, `arn:aws:sqs:${region}:${accountId}:${queueName}`)
        },
        /**
         * generate new async route for your stack, you mush provide listener along with the props
         * 
         * @param UOneStackScope scope 
         * @param String projectName 
         * @param String functionality 
         * @param UOneProps props 
         * @returns 
         */
        createDefaultProjectionEventsStack: (uoneSqs, sns, subs, projectName, whitelist, topicName,functionality, props) => {
            if(!uoneSqs) {
                throw new Error('uoneSns is required')
            }

            if(!sns) {
                throw new Error('sns is required')
            }

            if(!subs) {
                throw new Error('subs is required')
            }

            if(!projectName) {
                throw new Error('projectName is required')
            }

            if(!whitelist) {
                throw new Error('whitelist is required')
            }
            if(!functionality) {
                throw new Error('functionality is required')
            }

            if(!core) {
                throw new Error('core is required')
            }

            class DefaultProjectionEventsStack extends UOneStack {
                constructor(cdkContext, scope, id, props) {
                    super(cdkContext, scope, id, props)
                    this.queueName = props.queueName

                    const eventBus = uoneSns.fromTopicName(this)(props.topicName, cdkContext.envObj.account,  cdkContext.envObj.region)

                    const readDLQ = new sqs.Queue(this, `UOne-${props.projectName}-SQS-EventsDLQ${envName}`, {
                        queueName: `UOne-${props.projectName}-SQS-EventsDLQ${envName}`,
                        retentionPeriod: core.Duration.days(14)
                    })
                    const uoneTagHandler = UOneTagHandler(cdk.Tags, cdkContext)

                    uoneTagHandler.tag(readDLQ,"PROJECT",projectName);
                    uoneTagHandler.tag(readDLQ,"ENVIRONMENT",envName);
                    uoneTagHandler.tag(readDLQ,"FUNCTIONALITY",functionality);
                    uoneTagHandler.tag(readDLQ,"OWNER","Eng");

                    const readEvents = new sqs.Queue(this, `UOne-${props.projectName}-SQS-Events-${envName}`, {
                        queueName: props.queueName,
                        deadLetterQueue: {
                            queue: readDLQ,
                            maxReceiveCount: 1
                        }
                    })
                    uoneTagHandler.tag(
                        readEvents,
                        TAG_CONSTANTS.FUNCTIONALITY,
                        functionality
                    );

                    eventBus.addSubscription(
                        new subs.SqsSubscription(
                            readEvents,
                            {
                                filterPolicy: {
                                    bc: sns.SubscriptionFilter.stringFilter({
                                        whitelist: [props.whitelist]
                                    })
                                },
                                deadLetterQueue: readDLQ
                            }
                        )
                    )
                }
            }

            const stackProps = {
                queueName: `UOne-${projectName}-SQS-Events-${envName}`,
                topicName: topicName,
                projectName: projectName,
                whitelist: whitelist,
                ...props
            }

            const projEventStack = new DefaultProjectionEventsStack(cdkContext, scope, `UOne-${projectName}-DefaultProjectionEvents-${envName}`, stackProps);
            

            return projEventStack;
        }
    })
}

module.exports = {UOneSqs}
