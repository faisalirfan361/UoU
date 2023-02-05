/**
 * UOneCdkContext generates base for  AWS CDK Context 
 * @param UOneApp app 
 * @param String envName 
 * @returns 
 */
function UOneCdkContext(app,envName) {

    if(!envName)
        throw new Error('envName parameter is required')

    let envObj = {}
    const volatile = app.node.tryGetContext('volatile') === 'true'
    const subdomain = app.node.tryGetContext('subdomain')
    if(envName=="dev")
        envObj = {account: `${DEV_ACCOUNT}`, region:`${DEV_REGION}`}
    else if(envName=="demo")
        envObj = {account: `${DEMO_ACCOUNT}`, region:`${DEV_REGION}`}
    else if(envName=="prod")
        envObj = {account: `${PROD_ACCOUNT}`, region:`${DEV_REGION}`}
    else if(envName=="sbx")
        envObj = {account: `${SBX_ACCOUNT}`, region:`${DEV_REGION}`}
    else if(envName=="sbx-micro")
        envObj = {account: `${SBX_MICRO_ACCOUNT}`, region:`${DEV_REGION}`}
    return Object.freeze({
        envName,
        envObj,
        volatile,
        subdomain
    })
}

module.exports = {UOneCdkContext}