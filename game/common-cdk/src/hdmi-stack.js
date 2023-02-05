/**
 * This must be used in each stack as based class to proper tagging and logging
 * 
 * @param HDMIStackCore core 
 * @returns 
 */
function HdmiStackClass(core) {
    class HdmiStack extends core.Stack {
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

    return HdmiStack
}

module.exports = {HdmiStackClass}
