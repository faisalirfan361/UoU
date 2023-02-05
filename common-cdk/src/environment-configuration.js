const getAccount = () => process.env.CDK_DEFAULT_ACCOUNT
const getRegion = () => process.env.CDK_DEFAULT_REGION

module.exports = {
    getAccount,
    getRegion
}
