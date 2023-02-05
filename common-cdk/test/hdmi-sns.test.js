const {UOneSns} = require('../src/uone-sns')

describe('UOneSns', () => {
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

    describe('UOneSns required params', () => {
        it('should throw an error when the env is not provided', () => {
            expect(() => UOneSns())
                .toThrowError(new Error('cdkContext is required'))
        })

        it('should throw an error when the sns is not provided', () => {
            expect(() => UOneSns(cdkContext))
                .toThrowError(new Error('sns is required'))
        })
    })

    describe('fromTopicName', () => {

        it('should return an sns construct', () => {
            const sns = {
                Topic: {
                    fromTopicArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('UOne-Events-Topic')
                        expect(arn).toEqual('arn:aws:sns:us-east-1:54321:UOne-Events-Topic')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneSns = UOneSns(cdkContext, sns)
            const construct = uoneSns.fromTopicName(testScope)('UOne-Events-Topic', '54321', 'us-east-1')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should return an sns construct with a default region and account', () => {
            const sns = {
                Topic: {
                    fromTopicArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('UOne-Events-Topic')
                        expect(arn).toEqual('arn:aws:sns:us-west-2:12345:UOne-Events-Topic')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneSns = UOneSns(cdkContext, sns)
            const construct = uoneSns.fromTopicName(testScope)('UOne-Events-Topic')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should throw an error when the scope is not provided', () => {
            const uoneSns = UOneSns(cdkContext, {}, {})
            expect(() => uoneSns.fromTopicName()())
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the topic name is not provided', () => {
            const uoneSns = UOneSns(cdkContext, {}, {})
            expect(() => uoneSns.fromTopicName(testScope)(null, '12345'))
                .toThrowError(new Error('topicName is required'))
        })

        it('should throw an error when the accountId is not provided', () => {
            const uoneSns = UOneSns(cdkContext, {}, {})
            expect(() => uoneSns.fromTopicName(testScope)('UOne-Events-Topic', null))
                .toThrowError(new Error('accountId is required'))
        })
    })

    describe('newDefaultEventBusStack', () => {
        const UOneStack = class {
            constructor(cdkContext, scope, id, props) {
                this.cdkContext = cdkContext
                this.scope = scope
                this.id = id
                this.props = props
            }
        }

        it('should create a default event bus stack', () => {
            const sns = {
                Topic: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            topicName: 'UOne-TestStack-Topic-Test',
                            displayName: 'TestStack Event Bus'
                        })
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                }
            }

            const uoneSns = UOneSns(cdkContext, sns, UOneStack)

            const eventBusStack = uoneSns.newDefaultEventBusStack(testScope, 'TestStack')

            expect(eventBusStack.scope).toStrictEqual(testScope)
            expect(eventBusStack.id).toEqual('UOne-TestStack-EventBus-Test')
            expect(eventBusStack.topicName).toEqual('UOne-TestStack-Topic-Test')
            expect(eventBusStack.props).toStrictEqual({
                topicName: 'UOne-TestStack-Topic-Test',
                projectName: 'TestStack'
            })
        })

        it('should create a default event bus stack using custom props', () => {
            const sns = {
                Topic: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            topicName: 'UOne-TestStack-Topic-Test',
                            displayName: 'TestStack Event Bus'
                        })
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                }
            }

            const uoneSns = UOneSns(cdkContext, sns, UOneStack)

            const eventBusStack = uoneSns.newDefaultEventBusStack(testScope, 'TestStack', {
                someProp: true
            })

            expect(eventBusStack.scope).toStrictEqual(testScope)
            expect(eventBusStack.id).toEqual('UOne-TestStack-EventBus-Test')
            expect(eventBusStack.topicName).toEqual('UOne-TestStack-Topic-Test')
            expect(eventBusStack.props).toStrictEqual({
                topicName: 'UOne-TestStack-Topic-Test',
                projectName: 'TestStack',
                someProp: true
            })
        })

        it('should throw an error when the scope is not provided', () => {
            const uoneSns = UOneSns(cdkContext, {}, {}, {})
            expect(() => uoneSns.newDefaultEventBusStack())
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the projectName is not provided', () => {
            const uoneSns = UOneSns(cdkContext, {}, {}, {})
            expect(() => uoneSns.newDefaultEventBusStack(testScope))
                .toThrowError(new Error('projectName is required'))
        })
    })
})
