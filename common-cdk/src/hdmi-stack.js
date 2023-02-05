/**
 * This must be used in each stack as based class to proper tagging and logging
 * 
 * @param UOneStackCore core 
 * @returns 
 */
function UOneStackClass(core) {
    class UOneStack extends core.Stack {
        constructor(cdkContext, scope, id, props) {
            console.log("props ", props)
            console.log("cdkContext ", cdkContext)
            super(scope, id, {
                terminationProtection: !cdkContext.volatile,
                ...props,
                env: cdkContext.envObj
            })
        }
    }

    return UOneStack
}

module.exports = {UOneStackClass}
