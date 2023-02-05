const environment = require('../src/environment-configuration')

describe('environment', () => {
    process.env.CDK_DEFAULT_ACCOUNT = '12345'
    process.env.CDK_DEFAULT_REGION = 'us-west-2'

    it('should get the account', () => {
        expect(environment.getAccount()).toEqual('12345')
    })

    it('should get the region', () => {
        expect(environment.getRegion()).toEqual('us-west-2')
    })
})
