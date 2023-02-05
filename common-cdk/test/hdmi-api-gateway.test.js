const {HdmiApiGateway} = require('../src/hdmi-api-gateway')

/**
 * @TODO Faisal and Awais we should increase tests in this
 */
describe('HdmiApiGateway', () => {
    const cdkContext = {
        env: 'Dev'
    }
    it('should create an apigateway without using api key auth', () => {
        const testScope = {}

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', cdkContext)

        const api = hdmiApiGateway.buildApiGateway(['GET'], false)

        expect(api).toStrictEqual(new RestApi(testScope, 'Hdmi-Test-Gateway-API-Dev', {
            restApiName: 'Hdmi-Test-Gateway-API-Dev',
            deployOptions: {
                stageName: 'Dev'
            },
            defaultCorsPreflightOptions: {
                allowOrigins: [],
                allowMethods: ['GET'],
                allowHeaders: []
            }
        }))
    })

    it('should create an apigateway and bind the domain', () => {
        const testScope = {}

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi,
            DomainName: {
                fromDomainNameAttributes: (scope, id, options) => {
                    expect(scope).toEqual(testScope)
                    expect(id).toEqual('Hdmi-TestDomain-Dev')
                    expect(options).toStrictEqual({
                        domainName: 'test.heydaynow.com'
                    })
                    return options.domainName
                }
            },
            BasePathMapping: class {
                constructor(scope, id, options) {
                    expect(scope).toEqual(testScope)
                    expect(id).toEqual('Hdmi-TestBasePath-Dev')
                    expect(options.basePath).toEqual('dev')
                    expect(options.domainName).toEqual('test.heydaynow.com')
                }
            }
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', {
            ...cdkContext,
            subdomain: 'test.heydaynow.com'
        })

        const api = hdmiApiGateway.buildApiGateway(['GET'], false, 'dev')

        expect(api).toStrictEqual(new RestApi(testScope, 'Hdmi-Test-Gateway-API-Dev', {
            restApiName: 'Hdmi-Test-Gateway-API-Dev',
            deployOptions: {
                stageName: 'Dev'
            },
            defaultCorsPreflightOptions: {
                allowOrigins: [],
                allowMethods: ['GET'],
                allowHeaders: []
            }
        }))
    })

    it('should create an apigateway using the default api key auth config', () => {
        const testScope = {}

        class ApiKey {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-Key-Dev')
                expect(options.apiKeyName).toEqual('Hdmi-Test-Gateway-Key-Dev')
            }
        }

        class UsagePlan {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-UsagePlan-Dev')
                expect(options.name).toEqual('Hdmi-Test-Gateway-UsagePlan-Dev')
                expect(options.apiKey).toStrictEqual(new ApiKey(testScope, 'Hdmi-Test-Gateway-Key-Dev', {
                    apiKeyName: 'Hdmi-Test-Gateway-Key-Dev'
                }))
            }

            addApiStage(options) {
                expect(options.stage).toEqual('Dev')
            }
        }

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
                this.deploymentStage = 'Dev'
            }

            addApiKey(id, options) {
                const apiKey = new ApiKey(testScope, id, options)
                return apiKey
            }

            addUsagePlan(id, options) {
                const usagePlan = new UsagePlan(testScope, id, options)
                return usagePlan
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', cdkContext)

        const api = hdmiApiGateway.buildApiGateway(['GET'], true)

        expect(api).toStrictEqual(new RestApi(testScope, 'Hdmi-Test-Gateway-API-Dev', {
            restApiName: 'Hdmi-Test-Gateway-API-Dev',
            deployOptions: {
                stageName: 'Dev'
            },
            defaultCorsPreflightOptions: {
                allowOrigins: [],
                allowMethods: ['GET'],
                allowHeaders: []
            }
        }))
    })

    it('should create an apigateway using a value for the api key auth', () => {
        const testScope = {}

        class ApiKey {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-Key-Dev')
                expect(options).toStrictEqual({
                    apiKeyName: 'Hdmi-Test-Gateway-Key-Dev',
                    value: 'k3y'
                })
            }
        }

        class UsagePlan {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-UsagePlan-Dev')
                expect(options.name).toEqual('Hdmi-Test-Gateway-UsagePlan-Dev')
                expect(options.apiKey).toStrictEqual(new ApiKey(testScope, 'Hdmi-Test-Gateway-Key-Dev', {
                    apiKeyName: 'Hdmi-Test-Gateway-Key-Dev',
                    value: 'k3y'
                }))
            }

            addApiStage(options) {
                expect(options.stage).toEqual('Dev')
            }
        }

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
                this.deploymentStage = 'Dev'
            }

            addApiKey(id, options) {
                return new ApiKey(testScope, id, options)
            }

            addUsagePlan(id, options) {
                return new UsagePlan(testScope, id, options)
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', cdkContext)

        const api = hdmiApiGateway.buildApiGateway(['GET'], {apiKeyValue: 'k3y'})

        expect(api).toStrictEqual(new RestApi(testScope, 'Hdmi-Test-Gateway-API-Dev', {
            restApiName: 'Hdmi-Test-Gateway-API-Dev',
            deployOptions: {
                stageName: 'Dev'
            },
            defaultCorsPreflightOptions: {
                allowOrigins: [],
                allowMethods: ['GET'],
                allowHeaders: []
            }
        }))
    })

    it('should throw an error if basePath is not defined when using a subdomain', () => {
        const testScope = {}

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', {
            ...cdkContext,
            subdomain: 'test.heydaynow.com'
        })

        expect(() => hdmiApiGateway.buildApiGateway(['GET'], false)).toThrow(new Error('basePath parameter is required when using a subdomain'))
    })

    it('should throw an error if allowedMethods is not defined', () => {
        const testScope = {}

        class RestApi {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.restApiName).toEqual('Hdmi-Test-Gateway-API-Dev')
                expect(options.deployOptions.stageName).toEqual('Dev')
                expect(options.defaultCorsPreflightOptions).toStrictEqual({
                    allowOrigins: [],
                    allowMethods: ['GET'],
                    allowHeaders: []
                })
            }
        }


        const apigateway = {
            Cors: {
                ALL_ORIGINS: [],
                DEFAULT_HEADERS: []
            },
            RestApi: RestApi
        }

        const cdk = {
            apigateway
        }

        const hdmiApiGateway = HdmiApiGateway(cdk, testScope, 'Test', cdkContext)

        expect(() => hdmiApiGateway.buildApiGateway()).toThrow(new Error('allowedMethods parameter is required'))
    })

    it('should throw an error if projectName is not defined', () => {
        expect(() => HdmiApiGateway({}, {}, undefined, 'Dev')).toThrow(new Error('projectName parameter is required'))
    })

    it('should throw an error if env is not defined', () => {
        expect(() => HdmiApiGateway({}, {}, 'Test', undefined)).toThrow(new Error('cdkContext parameter is required'))
    })
})
