const {UOneStackClass} = require('../src/uone-stack')

describe('UOneStack', () => {
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
    it('should create a UOneStack class', () => {
        const UOneStack = UOneStackClass(core)

        const scope = {isThisAScope: true}

        const stack = new UOneStack(cdkContext, scope, 'stack', {property: false})

        expect(stack instanceof UOneStack).toBeTruthy()
        expect(stack.scope).toStrictEqual(scope)
        expect(stack.id).toEqual('stack')
        expect(stack.props).toStrictEqual({
            property: false,
            terminationProtection: false
        })
    })

    it('should create a UOneStack class with termination protection', () => {
        const UOneStack = UOneStackClass(core)

        const scope = {isThisAScope: true}

        const stack = new UOneStack(
            {
                ...cdkContext,
                volatile: false
            },
            scope,
            'stack',
            {property: false}
        )

        expect(stack instanceof UOneStack).toBeTruthy()
        expect(stack.scope).toStrictEqual(scope)
        expect(stack.id).toEqual('stack')
        expect(stack.props).toStrictEqual({
            property: false,
            terminationProtection: true
        })
    })
})
