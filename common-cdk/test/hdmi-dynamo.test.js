const {HdmiDynamo} = require('../src/hdmi-dynamo')

describe('HdmiDynamo', () => {
    const cdkContext = {
        env: 'Test',
        volatile: false
    }

    const testScope = {
        key: 'test',
        accountId: '12345'
    }

    describe('HdmiDynamo required params', () => {
        it('should throw an error when the env is not provided', () => {
            expect(() => HdmiDynamo())
                .toThrowError(new Error('cdkContext is required'))
        })

        it('should throw an error when the scope is not provided', () => {
            expect(() => HdmiDynamo(cdkContext))
                .toThrowError(new Error('scope is required'))
        })

        it('should throw an error when the dynamodb is not provided', () => {
            expect(() => HdmiDynamo(cdkContext, testScope))
                .toThrowError(new Error('dynamodb is required'))
        })
    })

    describe('newDefaultPersistenceStack', () => {
        const HdmiStack = class {
            constructor(cdkContext, scope, id, props) {
                this.cdkContext = cdkContext
                this.scope = scope
                this.id = id
                this.props = props
            }
        }

        const core = {
            RemovalPolicy: {
                DESTROY: 'Destroy',
                RETAIN: 'Retain'
            }
        }

        it('should create a default dynamo stack using the default table configuration', () => {
            const dynamodb = {
                Table: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            tableName: 'Hdmi-TestStack-Events-Test',
                            partitionKey: {name: 'contextId', type: 'String'},
                            sortKey: {name: 'eventDate', type: 'Number'},
                            billingMode: 'PayPerRequest',
                            removalPolicy: 'Retain'
                        })

                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                AttributeType: {
                    STRING: 'String',
                    NUMBER: 'Number'
                },
                BillingMode: {
                    PAY_PER_REQUEST: 'PayPerRequest'
                }
            }

            const hdmiDynamo = HdmiDynamo(cdkContext, testScope, dynamodb, core, HdmiStack)

            const defaultPersistenceStack = hdmiDynamo.newDefaultPersistenceStack('TestStack')

            expect(defaultPersistenceStack.scope).toStrictEqual(testScope)
            expect(defaultPersistenceStack.cdkContext).toStrictEqual(cdkContext)
            expect(defaultPersistenceStack.id).toEqual('Hdmi-TestStack-Write-Persistence-Test')
            expect(defaultPersistenceStack.tableName).toEqual('Hdmi-TestStack-Events-Test')
            expect(defaultPersistenceStack.props).toStrictEqual({
                tableName: 'Hdmi-TestStack-Events-Test'
            })
        })

        it('should create a volatile stack', () => {
            const volatileContext  = {
                env: 'Test',
                volatile: true
            }

            const core = {
                Stack: class {
                    constructor(scope, id, props) {
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                RemovalPolicy: {
                    DESTROY: 'Destroy',
                    RETAIN: 'Retain'
                }
            }

            const dynamodb = {
                Table: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            tableName: 'Hdmi-TestStack-Events-Test',
                            partitionKey: {name: 'contextId', type: 'String'},
                            sortKey: {name: 'eventDate', type: 'Number'},
                            billingMode: 'PayPerRequest',
                            removalPolicy: 'Destroy'
                        })

                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                AttributeType: {
                    STRING: 'String',
                    NUMBER: 'Number'
                },
                BillingMode: {
                    PAY_PER_REQUEST: 'PayPerRequest'
                }
            }

            const hdmiDynamo = HdmiDynamo(volatileContext, testScope, dynamodb, core, HdmiStack)

            const defaultPersistenceStack = hdmiDynamo.newDefaultPersistenceStack('TestStack')

            expect(defaultPersistenceStack.scope).toStrictEqual(testScope)
            expect(defaultPersistenceStack.id).toEqual('Hdmi-TestStack-Write-Persistence-Test')
            expect(defaultPersistenceStack.tableName).toEqual('Hdmi-TestStack-Events-Test')
            expect(defaultPersistenceStack.props).toStrictEqual({
                tableName: 'Hdmi-TestStack-Events-Test'
            })
        })

        it('should create a default dynamo stack using the default table configuration and custom props', () => {
            const dynamodb = {
                Table: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            tableName: 'Hdmi-TestStack-Events-Test',
                            partitionKey: {name: 'contextId', type: 'String'},
                            sortKey: {name: 'eventDate', type: 'Number'},
                            billingMode: 'PayPerRequest',
                            removalPolicy: 'Retain'
                        })

                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                AttributeType: {
                    STRING: 'String',
                    NUMBER: 'Number'
                },
                BillingMode: {
                    PAY_PER_REQUEST: 'PayPerRequest'
                }
            }

            const hdmiDynamo = HdmiDynamo(cdkContext, testScope, dynamodb, core, HdmiStack)

            const defaultPersistenceStack = hdmiDynamo.newDefaultPersistenceStack('TestStack', {}, {
                otherProp: 'Value'
            })

            expect(defaultPersistenceStack.scope).toStrictEqual(testScope)
            expect(defaultPersistenceStack.cdkContext).toStrictEqual(cdkContext)
            expect(defaultPersistenceStack.id).toEqual('Hdmi-TestStack-Write-Persistence-Test')
            expect(defaultPersistenceStack.tableName).toEqual('Hdmi-TestStack-Events-Test')
            expect(defaultPersistenceStack.props).toStrictEqual({
                tableName: 'Hdmi-TestStack-Events-Test',
                otherProp: 'Value'
            })
        })

        it('should create a default dynamo stack using a custom table configuration', () => {
            const dynamodb = {
                Table: class {
                    constructor(scope, id, props) {
                        expect(props).toStrictEqual({
                            tableName: 'Hdmi-TestStack-Events-Test',
                            partitionKey: {name: 'contextId', type: 'String'},
                            sortKey: {name: 'eventDate', type: 'Number'},
                            billingMode: 'PayPerRequest',
                            encryption: 'AWS_MANAGED',
                            removalPolicy: 'Retain'
                        })

                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                AttributeType: {
                    STRING: 'String',
                    NUMBER: 'Number'
                },
                BillingMode: {
                    PAY_PER_REQUEST: 'PayPerRequest'
                }
            }

            const hdmiDynamo = HdmiDynamo(cdkContext, testScope, dynamodb, core, HdmiStack)

            const defaultPersistenceStack = hdmiDynamo.newDefaultPersistenceStack('TestStack', {
                encryption: 'AWS_MANAGED'
            })

            expect(defaultPersistenceStack.scope).toStrictEqual(testScope)
            expect(defaultPersistenceStack.cdkContext).toStrictEqual(cdkContext)
            expect(defaultPersistenceStack.id).toEqual('Hdmi-TestStack-Write-Persistence-Test')
            expect(defaultPersistenceStack.tableName).toEqual('Hdmi-TestStack-Events-Test')
            expect(defaultPersistenceStack.props).toStrictEqual({
                tableName: 'Hdmi-TestStack-Events-Test'
            })
        })

        it('should throw an error when the projectName is not provided', () => {
            const hdmiDynamo = HdmiDynamo(cdkContext, {}, {}, {})
            expect(() => hdmiDynamo.newDefaultPersistenceStack())
                .toThrowError(new Error('projectName is required'))
        })
    })
})
