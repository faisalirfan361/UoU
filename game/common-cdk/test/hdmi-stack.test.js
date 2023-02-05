const {HdmiStackClass} = require('../src/hdmi-stack')

describe('HdmiStack', () => {
    const core = {
        Stack: class {
            constructor(scope, id, props) {
                this.scope = scope
                this.id = id
                this.props = props
            }
        }
    }
    const cdkContext = {
        env: 'Dev',
        volatile: true
    }
    it('should create a HdmiStack class', () => {
        const HdmiStack = HdmiStackClass(core)

        const scope = {isThisAScope: true}

        const stack = new HdmiStack(cdkContext, scope, 'stack', {property: false})

        expect(stack instanceof HdmiStack).toBeTruthy()
        expect(stack.scope).toStrictEqual(scope)
        expect(stack.id).toEqual('stack')
        expect(stack.props).toStrictEqual({
            property: false,
            terminationProtection: false
        })
    })

    it('should create a HdmiStack class with termination protection', () => {
        const HdmiStack = HdmiStackClass(core)

        const scope = {isThisAScope: true}

        const stack = new HdmiStack(
            {
                ...cdkContext,
                volatile: false
            },
            scope,
            'stack',
            {property: false}
        )

        expect(stack instanceof HdmiStack).toBeTruthy()
        expect(stack.scope).toStrictEqual(scope)
        expect(stack.id).toEqual('stack')
        expect(stack.props).toStrictEqual({
            property: false,
            terminationProtection: true
        })
    })
})
