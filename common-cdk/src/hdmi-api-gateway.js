const {match} = require('@othree.io/cerillo')
const awscdk = require('aws-cdk-lib');
const { UOneTagHandler } = require('./uone-tag-handler');
const { TAG_CONSTANTS } = require("./utils");
/**
 * UOneApiGateway will work as source for all API Gateway Micros
 * this will provide read and write path for each Micro's Gateway
 * 
 * @param UOneAwsCdkCore cdk 
 * @param UOneStackScope scope 
 * @param Strig projectName 
 * @param UOneCDKContext cdkContext 
 * @returns 
 */
function UOneApiGateway(cdk, scope, projectName, cdkContext) {
    if (!projectName) {
        throw new Error('projectName parameter is required')
    }

    if (!cdkContext) {
        throw new Error('cdkContext parameter is required')
    }

    const {envName, subdomain} = cdkContext
    const {apigateway} = cdk

    return Object.freeze({
        /**
         * This takes array of allowedMethods, basePath and apiKeyConfig
         * returns base API object wrapped in UOneApiGateway
         * 
         * @param String[] allowedMethods 
         * @param String apiKeyConfig 
         * @param String basePath (base path for the API)
         * @returns 
         */
        buildApiGateway: (allowedMethods, apiKeyConfig, basePath) => {
            if (!allowedMethods) {
                throw new Error('allowedMethods parameter is required')
            }

            /**
             * create root / base API for the micro read and write
             */
            const api = new apigateway.RestApi(scope, `UOne-${projectName}-Gateway-API-${envName}`, {
                restApiName: `UOne-${projectName}-Gateway-API-${envName}`,
                deployOptions: {
                    stageName: envName
                },
                defaultCorsPreflightOptions: {
                    allowOrigins: apigateway.Cors.ALL_ORIGINS,
                    allowMethods: allowedMethods,
                    allowHeaders: apigateway.Cors.DEFAULT_HEADERS
                }
            })

            /**
             * allows us to add tags and this will be used for logging, reporting and costing
             * It applies all basic tags from stack like project name, department and team
             */
            const uoneTagHandler = UOneTagHandler(awscdk.Tags, cdkContext)
            uoneTagHandler.tag(
                api,
                TAG_CONSTANTS.FUNCTIONALITY,
                "public-networking"
            );

            /**
             * this should just use the apikeyConfig and append key to root path for current service
             * adds usage plant o the service, API stage and base tags
             */
            if (apiKeyConfig) {
                const keyId = `UOne-${projectName}-Gateway-Key-${envName}`

                const apiKeyOptions = match(apiKeyConfig)
                    .when(_ => _.apiKeyValue).then(_ => ({
                        apiKeyName: keyId,
                        value: _.apiKeyValue
                    }))
                    .default(_ => ({
                        apiKeyName: keyId
                    }))
                    .get()

                const defaultApiKey = api.addApiKey(keyId, apiKeyOptions)

                // usage plan must be on root of each service gateway
                const defaultUsagePlan = api.addUsagePlan(`UOne-${projectName}-Gateway-UsagePlan-${envName}`, {
                    name: `UOne-${projectName}-Gateway-UsagePlan-${envName}`,
                    apiKey: defaultApiKey
                })

                defaultUsagePlan.addApiStage({
                    stage: api.deploymentStage
                })
                uoneTagHandler.tag(
                    defaultUsagePlan,
                    TAG_CONSTANTS.FUNCTIONALITY,
                    "public-networking"
                );

            }
            /**
             * we need to creat load domain, base API and append this requested service gateway API to it
             */
            if(subdomain) {
                if(!basePath) {
                    throw new Error('basePath parameter is required when using a subdomain')
                }

                const domain = apigateway.DomainName.fromDomainNameAttributes(scope, `UOne-${projectName}Domain-${envName}`, {
                    domainName: subdomain
                })

               const apiBasePath = new apigateway.BasePathMapping(scope, `UOne-${projectName}BasePath-${envName}`, {
                    basePath: basePath,
                    domainName: domain,
                    restApi: api
                })
                uoneTagHandler.tag(
                    apiBasePath,
                    TAG_CONSTANTS.FUNCTIONALITY,
                    "public-networking"
                );
                
            }

            return api
        }
    })
}

module.exports = {UOneApiGateway}
