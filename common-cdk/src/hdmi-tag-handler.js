const PREFIX = 'ST'
const { TAG_CONSTANTS } = require("./utils");

/**
 * This is base tagHandler to apply tags on each service,
 * stack, resource and more it auto applies all the base tags
 * 
 * @param UOneTagService tagService 
 * @param UOneCDKContext cdkContext 
 * @returns 
 */
function UOneTagHandler(tagService, cdkContext) {

    if (!tagService) {
        throw new Error('tag service is required')
    }

    if (!cdkContext) {
        throw new Error('cdkContext parameter is required')
    }

    const {envName} = cdkContext

    const addTag = (scope, tagKey, tagValue) => {
        tagService.of(scope).add(`${tagKey}` , tagValue)
    }

    return {
        tag: (scope, tagKey, tagValue) => {

            if (!scope) {
                throw new Error('scope parameter is required')
            }

            if (!tagKey) {
                throw new Error('tagKey parameter is required')
            }

            if (!tagValue) {
                throw new Error('tagValue parameter is required')
            }

            addTag(scope, tagKey , tagValue)

        },
        tagStack: (scope, name, description) => {

            if (!scope) {
                throw new Error('scope parameter is required')
            }

            if (!name) {
                throw new Error('stackName parameter is required')
            }

            if (!description) {
                throw new Error('description parameter is required')
            }

            addTag(scope, TAG_CONSTANTS.TEAM , TAG_CONSTANTS.SERVICES_TEAM)
            addTag(scope, TAG_CONSTANTS.ENVIRONMENT , envName.toUpperCase())
            addTag(scope, TAG_CONSTANTS.PROJECT , name.toUpperCase())
            addTag(scope, TAG_CONSTANTS.DESCRIPTION , description)

        }
    }
}

module.exports = {UOneTagHandler}
