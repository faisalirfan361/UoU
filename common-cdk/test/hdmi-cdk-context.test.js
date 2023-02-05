const {UOneCdkContext} = require('../src/uone-cdk-context')

describe('UOneCdkContext', () => {
    it('should return the cdk context', () => {
        const app = {
            node: {
                tryGetContext: context => {
                    if (context === 'env') {
                        return 'Prod'
                    } else if (context === 'volatile') {
                        return 'true'
                    } else if (context === 'subdomain') {
                        return 'test.heydaynow.com'
                    }
                }
            }
        }
        const context = UOneCdkContext(app)
        expect(context).toStrictEqual({
            env: 'Prod',
            volatile: true,
            subdomain: 'test.heydaynow.com'
        })
    })

    it('should return the default values for the cdk context', () => {
        const app = {
            node: {
                tryGetContext: context => {
                }
            }
        }

        const context = UOneCdkContext(app)

        expect(context).toStrictEqual({
            env: 'Dev',
            volatile: false,
            subdomain: undefined
        })
    })
})
