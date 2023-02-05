const environment = require('./environment-configuration')
const { UOneTagHandler } = require('./uone-tag-handler')
const cdk = require('aws-cdk-lib');
const { TAG_CONSTANTS } = require("./utils");
/**
 * Wrapper for AWS.Lambda provides wrappers and assigns VPC
 * and applies all necessary tags
 * 
 * @param UOneStackScope scope 
 * @param AWS.Lambda lambda 
 * @param HDNStackCore core 
 * @param HDNCdkContext cdkContext 
 * @returns 
 */
function UOneLambda(scope, lambda, core, cdkContext) {
    return Object.freeze({
        /**
         * Load function by Name
         * 
         * @param String functionName 
         * @returns 
         */
        fromFunctionName: (functionName) => {
            // Since we are using this function everywhere so we I am just replacing it here but probably
            // we do not need this function as it is available in cdk itself and somehow when we try to
            // load function by arn we get following issue
            // Error: Cannot modify permission to lambda function. Function is either imported or $LATEST version.
            // I am not removing accountId and region as it is probably being used in some other repo
            return lambda.Function.fromFunctionName(scope, functionName, functionName)
        },
        /**
         * Creates new AWS.Lambda with all the necessary taggings and VPC
         * 
         * @param String functionName 
         * @param String handler 
         * @param String environment 
         * @param String functionality 
         * @param String projectName 
         * @param UOneProps props 
         * @returns 
         */
        newFunction: (functionName, handler, environment = {},functionality, projectName, props = {}) => {
            if (!functionName) {
                throw new Error('functionName is required')
            }
            if (!projectName) {
                throw new Error('projectName is required')
            }
            if (!functionality) {
                throw new Error('functionality is required')
            }

            if (!handler) {
                throw new Error('handler is required')
            }

            const envs = {}

            if (cdkContext) {
                envs.envName = cdkContext.envName
            }

            const lambdaFunc = new lambda.Function(scope, functionName, {
                functionName: functionName,
                runtime: lambda.Runtime.NODEJS_14_X,
                code: lambda.Code.fromAsset('../codebase'),
                timeout: core.Duration.seconds(30),
                memorySize: 1024,
                handler: handler,
                environment: {
                    ...envs,
                    ...environment
                },
                ...props
            })
            const uoneTagHandler = UOneTagHandler(cdk.Tags, cdkContext)
            // append tags
            uoneTagHandler.tag(
                lambdaFunc,
                TAG_CONSTANTS.FUNCTIONALITY,
                functionality
            );
            return lambdaFunc;
        },
        /**
         * laod UOneLambda from given account by attributes
         * 
         * @param String functionName 
         * @param String accountId 
         * @param String region 
         * @returns 
         */
        fromFunctionAttributes: (functionName, accountId = environment.getAccount(), region = environment.getRegion()) => {
            if (cdkContext.envObj) {
                accountId = cdkContext.envObj.account;
                region = cdkContext.envObj.region;
            }
            if(!functionName) {
                throw new Error('functionName is required')
            }

            if(!accountId) {
                throw new Error('accountId is required')
            }
            const attrs = {
                functionArn: `arn:aws:lambda:${region}:${accountId}:function:${functionName}`,
                sameEnvironment: true
            }

            const lambdaFunc = lambda.Function.fromFunctionAttributes(scope, functionName, attrs)

            return lambdaFunc;
        }
    })
}

module.exports = {UOneLambda}