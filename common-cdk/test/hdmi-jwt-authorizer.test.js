const {UOneJwtAuthorizer} = require('../src/uone-jwt-authorizer')

describe('UOneJwtAuthorizer', () => {
    const cdkContext = {
        env: 'Dev',
        volatile: false
    }
    it('should get the authorizer', () => {
        const testScope = {}

        class ServicePrincipal {
            constructor(principal) {
                expect(principal).toEqual('apigateway.amazonaws.com')
                this.principal = principal
            }
        }

        class Role {
            constructor(scope, id, properties) {
                expect(scope).toEqual(testScope)
                expect(id).toStrictEqual('UOne-Test-Role-Authorizer-Dev')
                expect(properties).toStrictEqual({
                    assumedBy: new ServicePrincipal('apigateway.amazonaws.com')
                })

                this.principal = 'apigateway.amazonaws.com'
            }
        }

        const iam = {
            ServicePrincipal: ServicePrincipal,
            Role: Role
        }

        const expectedServicePrincipal = new ServicePrincipal('apigateway.amazonaws.com')
        const expectedRole = new Role(
            testScope,
            'UOne-Test-Role-Authorizer-Dev',
            {
                assumedBy: expectedServicePrincipal
            }
        )

        const uoneLambda = {
            fromFunctionName: (functionName) => {
                expect(functionName).toEqual('UOne-Test-ServiceFn-Authorizer-Dev')

                return {
                    id: '1337',
                    grantInvoke: (role) => {
                        expect(role).toStrictEqual(expectedRole)
                    }
                }
            }
        }

        class TokenAuthorizer {
            constructor(scope, id, options) {
                expect(scope).toEqual(testScope)
                expect(id).toEqual('UOne-Test-JwtAuthorizer-Dev')
                expect(options.handler.id).toEqual('1337')
                expect(options.assumeRole).toStrictEqual(expectedRole)

                this.id = '4u7h0r1z3r'
            }
        }

        const apigateway = {
            TokenAuthorizer: TokenAuthorizer
        }

        const cdk = {
            apigateway,
            iam
        }

        const uoneJwtAuthorizer = UOneJwtAuthorizer(cdk, testScope, 'Test', cdkContext, uoneLambda)

        const authorizer = uoneJwtAuthorizer.getAuthorizer('UOne-Test-ServiceFn-Authorizer-Dev')

        expect(authorizer).toStrictEqual(new TokenAuthorizer(
            testScope,
            'UOne-Test-JwtAuthorizer-Dev',
            {
                handler: {
                    id: '1337'
                },
                assumeRole: expectedRole
            }
        ))
    })

    it('should throw an error if projectName is not defined', () => {
        expect(() => UOneJwtAuthorizer({}, {}, undefined, cdkContext)).toThrow(new Error('projectName parameter is required'))
    })

    it('should throw an error if env is not defined', () => {
        expect(() => UOneJwtAuthorizer({}, {}, 'Test', undefined)).toThrow(new Error('cdkContext parameter is required'))
    })
})
