const {UOneEs} = require('../src/uone-es')

describe('UOneEs', () => {
    process.env.CDK_DEFAULT_ACCOUNT = '12345'
    process.env.CDK_DEFAULT_REGION = 'us-west-2'

    it('should get the es arn from name', () => {
        const uoneEs = UOneEs()
        const arn = uoneEs.getArnFromName('test', '0001', 'us-east-1')
        expect(arn).toEqual('arn:aws:es:us-east-1:0001:domain/test')
    })

    it('should get the es arn from name using the default values', () => {
        const uoneEs = UOneEs()
        const arn = uoneEs.getArnFromName('test')
        expect(arn).toEqual('arn:aws:es:us-west-2:12345:domain/test')
    })
})