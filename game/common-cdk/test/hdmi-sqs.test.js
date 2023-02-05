const {HdmiSqs} = require('../src/hdmi-sqs')

describe('HdmiSqs', () => {
    process.env.CDK_DEFAULT_ACCOUNT = '12345'
    process.env.CDK_DEFAULT_REGION = 'us-west-2'

    const cdkContext = {
        env: 'Test',
        volatile: true
    }

    const testScope = {
        key: 'test',
        accountId: '12345'
    }

    describe('HdmiSqs required params', () => {
        it('should throw an error when the cdkContext is not provided', () => {
            expect(() => HdmiSqs())
                .toThrowError(new Error('cdkContext is required'))
        })

        it('should throw an error when the scope is not provided', () => {
            expect(() => HdmiSqs(cdkContext))
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the sqs is not provided', () => {
            expect(() => HdmiSqs(cdkContext, testScope))
                .toThrowError(new Error('sqs is required'))
        })
    })

    describe('fromTopicName', () => {
        it('should return an sqs construct', () => {
            const sqs = {
                Queue: {
                    fromQueueArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('Hdmi-SQS-Event')
                        expect(arn).toEqual('arn:aws:sqs:us-east-1:54321:Hdmi-SQS-Event')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const hdmiSqs = HdmiSqs(cdkContext, testScope, sqs, {})
            const construct = hdmiSqs.fromQueueName('Hdmi-SQS-Event', '54321', 'us-east-1')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should return an sqs construct with a default region and account', () => {
            const sqs = {
                Queue: {
                    fromQueueArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('Hdmi-SQS-Event')
                        expect(arn).toEqual('arn:aws:sqs:us-west-2:12345:Hdmi-SQS-Event')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const hdmiSqs = HdmiSqs(cdkContext, testScope, sqs, {})
            const construct = hdmiSqs.fromQueueName('Hdmi-SQS-Event')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should throw an error when the queue name is not provided', () => {
            const hdmiSqs = HdmiSqs({}, {}, {})
            expect(() => hdmiSqs.fromQueueName(null, '12345'))
                .toThrowError(new Error('queueName is required'))
        })

        it('should throw an error when the accountId is not provided', () => {
            const hdmiSqs = HdmiSqs({}, {}, {})
            expect(() => hdmiSqs.fromQueueName('Hdmi-SQS-Event', null))
                .toThrowError(new Error('accountId is required'))
        })
    })

    describe('createDefaultProjectionEventsStack', () => {
        const HdmiStack = class {
            constructor(cdkContext, scope, id, props) {
                this.cdkContext = cdkContext
                this.scope = scope
                this.id = id
                this.props = props
            }
        }

        it('should create a default projection events stack', () => {
            const core = {
                Duration: {
                    days: (duration) => {
                        return {duration}
                    }
                }
            }

            const hdmiSns = {
                fromTopicName: (scope) => (topicName) => {
                    expect(topicName).toEqual('Hdmi-TestProject-Topic-Test')
                    expect(scope.id).toEqual('Hdmi-TestProject-DefaultProjectionEvents-Test')
                    return {
                        addSubscription: (sqsSubscription) => {
                            return {
                                id: 'sqsSubscription'
                            }
                        }
                    }
                }
            }

            class Queue {
                constructor(scope, id, props) {
                    if (id === 'Hdmi-TestProject-SQS-EventsDLQTest') {
                        expect(props).toStrictEqual({
                            queueName: 'Hdmi-TestProject-SQS-EventsDLQTest',
                            retentionPeriod: core.Duration.days(14)
                        })
                    } else if (id === 'Hdmi-TestProject-SQS-Events-Test') {
                        expect(props).toStrictEqual({
                            queueName: 'Hdmi-TestProject-SQS-Events-Test',
                            deadLetterQueue: {
                                queue: new Queue(scope, 'Hdmi-TestProject-SQS-EventsDLQTest', {
                                    queueName: 'Hdmi-TestProject-SQS-EventsDLQTest',
                                    retentionPeriod: core.Duration.days(14)
                                }),
                                maxReceiveCount: 1
                            }
                        })
                    }

                    this.scope = scope
                    this.id = id
                    this.props = props
                }
            }

            const sqs = {
                Queue: Queue
            }

            const sns = {
                SubscriptionFilter: {
                    stringFilter: (props) => {
                        expect(props).toStrictEqual({
                            whitelist: ['test']
                        })
                        return props
                    }
                }
            }

            const subs = {
                SqsSubscription: class {
                    constructor(queue, props) {
                        expect(queue.id).toEqual('Hdmi-TestProject-SQS-Events-Test')
                        expect(props.filterPolicy).toStrictEqual({
                            bc: {
                                whitelist: ['test']
                            }
                        })
                        expect(props.deadLetterQueue.id).toEqual('Hdmi-TestProject-SQS-EventsDLQTest')
                        this.queue = queue
                        this.props = props
                    }
                }
            }

            const hdmiSqs = HdmiSqs(cdkContext, testScope, sqs, core, HdmiStack)

            const projectionEventsStack = hdmiSqs.createDefaultProjectionEventsStack(hdmiSns, sns, subs, 'TestProject', 'test', 'Hdmi-TestProject-Topic-Test', {})
            expect(projectionEventsStack.scope).toStrictEqual(testScope)
            expect(projectionEventsStack.id).toEqual('Hdmi-TestProject-DefaultProjectionEvents-Test')
            expect(projectionEventsStack.queueName).toEqual('Hdmi-TestProject-SQS-Events-Test')
            expect(projectionEventsStack.props).toStrictEqual({
                queueName: 'Hdmi-TestProject-SQS-Events-Test',
                topicName: 'Hdmi-TestProject-Topic-Test',
                projectName: 'TestProject',
                whitelist: 'test'
            })
        })

        it('should create a default projection events stack using custom props', () => {
            const core = {
                Duration: {
                    days: (duration) => {
                        return {duration}
                    }
                }
            }

            const hdmiSns = {
                fromTopicName: (scope) => (topicName) => {
                    expect(topicName).toEqual('Hdmi-TestProject-Topic-Test')
                    expect(scope.id).toEqual('Hdmi-TestProject-DefaultProjectionEvents-Test')
                    return {
                        addSubscription: (sqsSubscription) => {
                            return {
                                id: 'sqsSubscription'
                            }
                        }
                    }
                }
            }

            class Queue {
                constructor(scope, id, props) {
                    if (id === 'Hdmi-TestProject-SQS-EventsDLQTest') {
                        expect(props).toStrictEqual({
                            queueName: 'Hdmi-TestProject-SQS-EventsDLQTest',
                            retentionPeriod: core.Duration.days(14)
                        })
                    } else if (id === 'Hdmi-TestProject-SQS-Events-Test') {
                        expect(props).toStrictEqual({
                            queueName: 'Hdmi-TestProject-SQS-Events-Test',
                            deadLetterQueue: {
                                queue: new Queue(scope, 'Hdmi-TestProject-SQS-EventsDLQTest', {
                                    queueName: 'Hdmi-TestProject-SQS-EventsDLQTest',
                                    retentionPeriod: core.Duration.days(14)
                                }),
                                maxReceiveCount: 1
                            }
                        })
                    }

                    this.scope = scope
                    this.id = id
                    this.props = props
                }
            }

            const sqs = {
                Queue: Queue
            }

            const sns = {
                SubscriptionFilter: {
                    stringFilter: (props) => {
                        expect(props).toStrictEqual({
                            whitelist: ['test']
                        })
                        return props
                    }
                }
            }

            const subs = {
                SqsSubscription: class {
                    constructor(queue, props) {
                        expect(queue.id).toEqual('Hdmi-TestProject-SQS-Events-Test')
                        expect(props.filterPolicy).toStrictEqual({
                            bc: {
                                whitelist: ['test']
                            }
                        })
                        expect(props.deadLetterQueue.id).toEqual('Hdmi-TestProject-SQS-EventsDLQTest')
                        this.queue = queue
                        this.props = props
                    }
                }
            }

            const hdmiSqs = HdmiSqs(cdkContext, testScope, sqs, core, HdmiStack)

            const projectionEventsStack = hdmiSqs.createDefaultProjectionEventsStack(hdmiSns, sns, subs, 'TestProject', 'test', 'Hdmi-TestProject-Topic-Test', {
                name: 'TestName'
            })
            expect(projectionEventsStack.scope).toStrictEqual(testScope)
            expect(projectionEventsStack.id).toEqual('Hdmi-TestProject-DefaultProjectionEvents-Test')
            expect(projectionEventsStack.queueName).toEqual('Hdmi-TestProject-SQS-Events-Test')
            expect(projectionEventsStack.props).toStrictEqual({
                queueName: 'Hdmi-TestProject-SQS-Events-Test',
                topicName: 'Hdmi-TestProject-Topic-Test',
                projectName: 'TestProject',
                whitelist: 'test',
                name: 'TestName'
            })
        })

        it('should throw an error when hdmiSns is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {}, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack())
                .toThrowError(new Error('hdmiSns is required'))
        })

        it('should throw an error when sns is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {}, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack({}))
                .toThrowError(new Error('sns is required'))
        })

        it('should throw an error when subs is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {}, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack({}, {}))
                .toThrowError(new Error('subs is required'))
        })

        it('should throw an error when projectName is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {}, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack({}, {}, {}))
                .toThrowError(new Error('projectName is required'))
        })

        it('should throw an error when whitelist is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {}, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack({}, {}, {}, 'TestProject'))
                .toThrowError(new Error('whitelist is required'))
        })

        it('should throw an error when core is not provided', () => {
            const hdmiSqs = HdmiSqs(cdkContext, testScope, {})
            expect(() => hdmiSqs.createDefaultProjectionEventsStack({}, {}, {}, 'TestProject', 'test'))
                .toThrowError(new Error('core is required'))
        })
    })
})
