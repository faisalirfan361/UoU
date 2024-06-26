const {UOneLambda} = require('../src/uone-lambda')

describe('UOneLambda', () => {
    process.env.CDK_DEFAULT_ACCOUNT = '12345'
    process.env.CDK_DEFAULT_REGION = 'us-west-2'

    const testScope = {
        key: 'test',
        accountId: '12345'
    }

    describe('fromFunctionName', () => {
        it('should return a lambda construct', () => {
            const lambda = {
                Function: {
                    fromFunctionArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('testFunction')
                        expect(arn).toEqual('arn:aws:lambda:us-east-1:54321:function:testFunction')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneLambda = UOneLambda(testScope, lambda)
            const construct = uoneLambda.fromFunctionName('testFunction', '54321', 'us-east-1')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should return a lambda construct using the default region and account id', () => {

            const lambda = {
                Function: {
                    fromFunctionArn: (scope, id, arn) => {
                        expect(scope).toStrictEqual(testScope)
                        expect(id).toEqual('testFunction')
                        expect(arn).toEqual('arn:aws:lambda:us-west-2:12345:function:testFunction')
                        return {
                            id: '0001'
                        }
                    }
                }
            }

            const uoneLambda = UOneLambda(testScope, lambda)
            const construct = uoneLambda.fromFunctionName('testFunction')
            expect(construct).toStrictEqual({
                id: '0001'
            })
        })

        it('should try to return a lambda construct and throw an error when the function name is not provided', () => {
            const uoneLambda = UOneLambda({}, {})
            expect(() => uoneLambda.fromFunctionName(null, '12345'))
                .toThrowError(new Error('functionName is required'))
        })

        it('should try to return a lambda construct and throw an error when the accountId is not provided', () => {
            const uoneLambda = UOneLambda({}, {})
            expect(() => uoneLambda.fromFunctionName('testFunction', null))
                .toThrowError(new Error('accountId is required'))
        })
    })

    describe('newFunction', () => {
        it('should create a new function with the default values', () => {
            const lambda = {
                Function: class {
                    constructor(scope, id, props) {
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                Code: {
                    fromAsset: (codePath) => {
                        return {codePath}
                    }
                },
                Runtime: {
                    NODEJS_14_X: 'NODEJS_14_X'
                }
            }
            const core = {
                Duration: {
                    seconds: (duration) => {
                        return {duration}
                    }
                }
            }
            const uoneLambda = UOneLambda(testScope, lambda, core)

            const fn = uoneLambda.newFunction('UOne-New-Fn', './src/path/to/entrypoint.handler')

            expect(fn.scope).toStrictEqual(testScope)
            expect(fn.id).toEqual('UOne-New-Fn')
            expect(fn.props).toStrictEqual({
                functionName: 'UOne-New-Fn',
                runtime: lambda.Runtime.NODEJS_14_X,
                code: lambda.Code.fromAsset('../codebase'),
                timeout: core.Duration.seconds(30),
                memorySize: 256,
                handler: './src/path/to/entrypoint.handler',
                environment: {
                }
            })
        })

        it('should throw an error if the function name is not provided', () => {
            const uoneLambda = UOneLambda(testScope, {}, {})

            expect(() => uoneLambda.newFunction()).toThrowError(new Error('functionName is required'))
        })

        it('should throw an error if the handler is not provided', () => {
            const uoneLambda = UOneLambda(testScope, {}, {})

            expect(() => uoneLambda.newFunction('UOne-Function-Name')).toThrowError(new Error('handler is required'))
        })

        it('should create a new function with the environment and props provided', () => {
            const lambda = {
                Function: class {
                    constructor(scope, id, props) {
                        this.scope = scope
                        this.id = id
                        this.props = props
                    }
                },
                Code: {
                    fromAsset: (codePath) => {
                        return {codePath}
                    }
                },
                Runtime: {
                    NODEJS_14_X: 'NODEJS_14_X'
                }
            }
            const core = {
                Duration: {
                    seconds: (duration) => {
                        return {duration}
                    }
                }
            }
            const uoneLambda = UOneLambda(testScope, lambda, core)

            const fn = uoneLambda.newFunction(
                'UOne-New-Fn',
                './src/path/to/entrypoint.handler',
                {
                    SOME_CONFIG: 'configuration.value'
                },
                {
                    memorySize: 512
                })

            expect(fn.scope).toStrictEqual(testScope)
            expect(fn.id).toEqual('UOne-New-Fn')
            expect(fn.props).toStrictEqual({
                functionName: 'UOne-New-Fn',
                runtime: lambda.Runtime.NODEJS_14_X,
                code: lambda.Code.fromAsset('../codebase'),
                timeout: core.Duration.seconds(30),
                memorySize: 512,
                handler: './src/path/to/entrypoint.handler',
                environment: {
                    SOME_CONFIG: 'configuration.value'
                }
            })
        })
    })
})