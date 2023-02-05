const environment = require('./environment-configuration')
/**
 * Basic wrapped for AWS ES (Elastich Search) 
 * returns object with base utils for ES
 * @returns 
 */
function HdmiEs() {
    return Object.freeze({
        getArnFromName: (name, accountId = environment.getAccount(), region = environment.getRegion()) => {
            return `arn:aws:es:${region}:${accountId}:domain/${name}`
        }
    })
}

module.exports = {HdmiEs}