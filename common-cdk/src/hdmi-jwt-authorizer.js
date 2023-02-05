/**
 * this must be the only source for auth across all services 
 * We must append or discuss all change sin this accross the teams
 * 
 * @param HDNCdkContext cdk 
 * @param HDMIStackScope scope 
 * @param String projectName 
 * @param AWSContext cdkContext 
 * @param HdmiLambda hdmiLambda 
 * @returns 
 */
function HdmiJwtAuthorizer(cdk, scope, projectName, cdkContext, hdmiLambda) {
    const { apigateway, iam } = cdk

    if (!projectName) {
        throw new Error('projectName parameter is required')
    }

    if (!cdkContext) {
        throw new Error('cdkContext parameter is required')
    }

    const { envName } = cdkContext

    return {
        /**
         * provide custom authorizer which musy implement HDMIAuthorizer Lambda
         * 
         * @param Function authorizerFnImport 
         * @returns 
         */
        getAuthorizer: (authorizerFnImport) => {
            const role = new iam.Role(scope, `Hdmi-${projectName}-Role-Authorizer-${envName}`, {
                assumedBy: new iam.ServicePrincipal('apigateway.amazonaws.com'),
            })

            const authorizerHandler = hdmiLambda.fromFunctionName(authorizerFnImport)
            authorizerHandler.grantInvoke(role)

            const authorizer = new apigateway.TokenAuthorizer(
                scope,
                `Hdmi-${projectName}-JwtAuthorizer-${envName}`,
                {
                    handler: authorizerHandler,
                    assumeRole: role
                }
            )

            return authorizer
        }
    }
}

module.exports = { HdmiJwtAuthorizer }
