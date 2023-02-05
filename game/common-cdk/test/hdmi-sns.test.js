const {HdmiSns} = require('../src/hdmi-sns')

describe('HdmiSns', () => {
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

    describe('HdmiSns required params', () => {
        it('should throw an error when the env is not provided', () => {
            expect(() => HdmiSns())
                .toThrowError(new Error('cdkContext is required'))
        })

        it('should throw an error when the sns is not provided', () => {
            expect(() => HdmiSns(cdkContext))
                .toThrowError(new Error('sns is required'))
        })
    })

    describe('fromTopicName', () => {

        it('should return an sns construct', () => {
            const sns = {
                Topic: {
                    fromTopicArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('Hdmi-Events-Topic')
                        expect(arn).toEqual('arn:aws:sns:us-east-1:54321:Hdmi-Events-Topic')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const hdmiSns = HdmiSns(cdkContext, sns)
            const construct = hdmiSns.fromTopicName(testScope)('Hdmi-Events-Topic', '54321', 'us-east-1')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should return an sns construct with a default region and account', () => {
            const sns = {
                Topic: {
                    fromTopicArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('Hdmi-Events-Topic')
                        expect(arn).toEqual('arn:aws:sns:us-west-2:12345:Hdmi-Events-Topic')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const hdmiSns = HdmiSns(cdkContext, sns)
            const construct = hdmiSns.fromTopicName(testScope)('Hdmi-Events-Topic')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should throw an error when the scope is not provided', () => {
            const hdmiSns = HdmiSns(cdkContext, {}, {})
            expect(() => hdmiSns.fromTopicName()())
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the topic name is not provided', () => {
            const hdmiSns = HdmiSns(cdkContext, {}, {})
            expect(() => hdmiSns.fromTopicName(testScope)(null, '12345'))
                .toThrowError(new Error('topicName is required'))
        })

        it('should throw an error when the accountId is not provided', () => {
            const hdmiSns = HdmiSns(cdkContext, {}, {})
            expect(() => hdmiSns.fromTopicName(testScope)('Hdmi-Events-Topic', null))
                .toThrowError(new Error('accountId is required'))
        })
    })

    describe('newDefaultEventBusStack', () => {
        const HdmiStack = class {
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
                            topicName: 'Hdmi-TestStack-Topic-Test',
                            displayName: 'TestStack Event Bus'
                        })
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                }
            }

            const hdmiSns = HdmiSns(cdkContext, sns, HdmiStack)

            const eventBusStack = hdmiSns.newDefaultEventBusStack(testScope, 'TestStack')

            expect(eventBusStack.scope).toStrictEqual(testScope)
            expect(eventBusStack.id).toEqual('Hdmi-TestStack-EventBus-Test')
            expect(eventBusStack.topicName).toEqual('Hdmi-TestStack-Topic-Test')
            expect(eventBusStack.props).toStrictEqual({
                topicName: 'Hdmi-TestStack-Topic-Test',
                projectName: 'TestStack'
            })
        })

        it('should create a default event bus stack using custom props', () => {
            const sns = {
                Topic: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            topicName: 'Hdmi-TestStack-Topic-Test',
                            displayName: 'TestStack Event Bus'
                        })
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                }
            }

            const hdmiSns = HdmiSns(cdkContext, sns, HdmiStack)

            const eventBusStack = hdmiSns.newDefaultEventBusStack(testScope, 'TestStack', {
                someProp: true
            })

            expect(eventBusStack.scope).toStrictEqual(testScope)
            expect(eventBusStack.id).toEqual('Hdmi-TestStack-EventBus-Test')
            expect(eventBusStack.topicName).toEqual('Hdmi-TestStack-Topic-Test')
            expect(eventBusStack.props).toStrictEqual({
                topicName: 'Hdmi-TestStack-Topic-Test',
                projectName: 'TestStack',
                someProp: true
            })
        })

        it('should throw an error when the scope is not provided', () => {
            const hdmiSns = HdmiSns(cdkContext, {}, {}, {})
            expect(() => hdmiSns.newDefaultEventBusStack())
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the projectName is not provided', () => {
            const hdmiSns = HdmiSns(cdkContext, {}, {}, {})
            expect(() => hdmiSns.newDefaultEventBusStack(testScope))
                .toThrowError(new Error('projectName is required'))
        })
    })
})
