const {UOneSqs} = require('../src/uone-sqs')

describe('UOneSqs', () => {
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

    describe('UOneSqs required params', () => {
        it('should throw an error when the cdkContext is not provided', () => {
            expect(() => UOneSqs())
                .toThrowError(new Error('cdkContext is required'))
        })

        it('should throw an error when the scope is not provided', () => {
            expect(() => UOneSqs(cdkContext))
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the sqs is not provided', () => {
            expect(() => UOneSqs(cdkContext, testScope))
                .toThrowError(new Error('sqs is required'))
        })
    })

    describe('fromTopicName', () => {
        it('should return an sqs construct', () => {
            const sqs = {
                Queue: {
                    fromQueueArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('UOne-SQS-Event')
                        expect(arn).toEqual('arn:aws:sqs:us-east-1:54321:UOne-SQS-Event')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneSqs = UOneSqs(cdkContext, testScope, sqs, {})
            const construct = uoneSqs.fromQueueName('UOne-SQS-Event', '54321', 'us-east-1')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should return an sqs construct with a default region and account', () => {
            const sqs = {
                Queue: {
                    fromQueueArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('UOne-SQS-Event')
                        expect(arn).toEqual('arn:aws:sqs:us-west-2:12345:UOne-SQS-Event')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneSqs = UOneSqs(cdkContext, testScope, sqs, {})
            const construct = uoneSqs.fromQueueName('UOne-SQS-Event')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should throw an error when the queue name is not provided', () => {
            const uoneSqs = UOneSqs({}, {}, {})
            expect(() => uoneSqs.fromQueueName(null, '12345'))
                .toThrowError(new Error('queueName is required'))
        })

        it('should throw an error when the accountId is not provided', () => {
            const uoneSqs = UOneSqs({}, {}, {})
            expect(() => uoneSqs.fromQueueName('UOne-SQS-Event', null))
                .toThrowError(new Error('accountId is required'))
        })
    })

    describe('createDefaultProjectionEventsStack', () => {
        const UOneStack = class {
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

            const uoneSns = {
                fromTopicName: (scope) => (topicName) => {
                    expect(topicName).toEqual('UOne-TestProject-Topic-Test')
                    expect(scope.id).toEqual('UOne-TestProject-DefaultProjectionEvents-Test')
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
                    if (id === 'UOne-TestProject-SQS-EventsDLQTest') {
                        expect(props).toStrictEqual({
                            queueName: 'UOne-TestProject-SQS-EventsDLQTest',
                            retentionPeriod: core.Duration.days(14)
                        })
                    } else if (id === 'UOne-TestProject-SQS-Events-Test') {
                        expect(props).toStrictEqual({
                            queueName: 'UOne-TestProject-SQS-Events-Test',
                            deadLetterQueue: {
                                queue: new Queue(scope, 'UOne-TestProject-SQS-EventsDLQTest', {
                                    queueName: 'UOne-TestProject-SQS-EventsDLQTest',
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
                        expect(queue.id).toEqual('UOne-TestProject-SQS-Events-Test')
                        expect(props.filterPolicy).toStrictEqual({
                            bc: {
                                whitelist: ['test']
                            }
                        })
                        expect(props.deadLetterQueue.id).toEqual('UOne-TestProject-SQS-EventsDLQTest')
                        this.queue = queue
                        this.props = props
                    }
                }
            }

            const uoneSqs = UOneSqs(cdkContext, testScope, sqs, core, UOneStack)

            const projectionEventsStack = uoneSqs.createDefaultProjectionEventsStack(uoneSns, sns, subs, 'TestProject', 'test', 'UOne-TestProject-Topic-Test', {})
            expect(projectionEventsStack.scope).toStrictEqual(testScope)
            expect(projectionEventsStack.id).toEqual('UOne-TestProject-DefaultProjectionEvents-Test')
            expect(projectionEventsStack.queueName).toEqual('UOne-TestProject-SQS-Events-Test')
            expect(projectionEventsStack.props).toStrictEqual({
                queueName: 'UOne-TestProject-SQS-Events-Test',
                topicName: 'UOne-TestProject-Topic-Test',
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

            const uoneSns = {
                fromTopicName: (scope) => (topicName) => {
                    expect(topicName).toEqual('UOne-TestProject-Topic-Test')
                    expect(scope.id).toEqual('UOne-TestProject-DefaultProjectionEvents-Test')
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
                    if (id === 'UOne-TestProject-SQS-EventsDLQTest') {
                        expect(props).toStrictEqual({
                            queueName: 'UOne-TestProject-SQS-EventsDLQTest',
                            retentionPeriod: core.Duration.days(14)
                        })
                    } else if (id === 'UOne-TestProject-SQS-Events-Test') {
                        expect(props).toStrictEqual({
                            queueName: 'UOne-TestProject-SQS-Events-Test',
                            deadLetterQueue: {
                                queue: new Queue(scope, 'UOne-TestProject-SQS-EventsDLQTest', {
                                    queueName: 'UOne-TestProject-SQS-EventsDLQTest',
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
                        expect(queue.id).toEqual('UOne-TestProject-SQS-Events-Test')
                        expect(props.filterPolicy).toStrictEqual({
                            bc: {
                                whitelist: ['test']
                            }
                        })
                        expect(props.deadLetterQueue.id).toEqual('UOne-TestProject-SQS-EventsDLQTest')
                        this.queue = queue
                        this.props = props
                    }
                }
            }

            const uoneSqs = UOneSqs(cdkContext, testScope, sqs, core, UOneStack)

            const projectionEventsStack = uoneSqs.createDefaultProjectionEventsStack(uoneSns, sns, subs, 'TestProject', 'test', 'UOne-TestProject-Topic-Test', {
                name: 'TestName'
            })
            expect(projectionEventsStack.scope).toStrictEqual(testScope)
            expect(projectionEventsStack.id).toEqual('UOne-TestProject-DefaultProjectionEvents-Test')
            expect(projectionEventsStack.queueName).toEqual('UOne-TestProject-SQS-Events-Test')
            expect(projectionEventsStack.props).toStrictEqual({
                queueName: 'UOne-TestProject-SQS-Events-Test',
                topicName: 'UOne-TestProject-Topic-Test',
                projectName: 'TestProject',
                whitelist: 'test',
                name: 'TestName'
            })
        })

        it('should throw an error when uoneSns is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {}, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack())
                .toThrowError(new Error('uoneSns is required'))
        })

        it('should throw an error when sns is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {}, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack({}))
                .toThrowError(new Error('sns is required'))
        })

        it('should throw an error when subs is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {}, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack({}, {}))
                .toThrowError(new Error('subs is required'))
        })

        it('should throw an error when projectName is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {}, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack({}, {}, {}))
                .toThrowError(new Error('projectName is required'))
        })

        it('should throw an error when whitelist is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {}, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack({}, {}, {}, 'TestProject'))
                .toThrowError(new Error('whitelist is required'))
        })

        it('should throw an error when core is not provided', () => {
            const uoneSqs = UOneSqs(cdkContext, testScope, {})
            expect(() => uoneSqs.createDefaultProjectionEventsStack({}, {}, {}, 'TestProject', 'test'))
                .toThrowError(new Error('core is required'))
        })
    })
})
