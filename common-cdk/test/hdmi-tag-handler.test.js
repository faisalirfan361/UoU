const {UOneTagHandler} = require('../src/uone-tag-handler')

describe('UOneTagHandler', () => {

    const cdkContext = {
        env: 'Testing',
        volatile: false
    }

    it('should add single tag', () => {
        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        uoneTagHandler.tag(testScope, 'testKey', 'testValue')

        expect(tagService.of).toBeCalledTimes(1)
        expect(tagService.add).toBeCalledTimes(1)
    })

    it('should try to add single tag with missing scope', () => {

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        expect(() => uoneTagHandler.tag(null, 'tagKey', 'testValue'))
            .toThrowError(new Error('scope parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should try to add single tag with missing tag key', () => {
        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        expect(() => uoneTagHandler.tag(testScope, '', 'testValue'))
            .toThrowError(new Error('tagKey parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should try to add single tag with missing tag value', () => {
        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        expect(() => uoneTagHandler.tag(testScope, 'tagKey', ''))
            .toThrowError(new Error('tagValue parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should tag a stack', () => {
        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        uoneTagHandler.tagStack(testScope, 'UOne-Testing')

        expect(tagService.of).toBeCalledTimes(3)
        expect(tagService.add).toBeCalledTimes(3)
    })

    it('should try to tag a stack with missing scope', () => {

        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        expect(() => uoneTagHandler.tagStack(null, 'UOne-Test'))
            .toThrowError(new Error('scope parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should try to tag a stack with missing stack name', () => {

        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const uoneTagHandler = UOneTagHandler(tagService, cdkContext)

        expect(() => uoneTagHandler.tagStack(testScope, ''))
            .toThrowError(new Error('stackName parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should try to tag with missing tag service', () => {

        expect(() => UOneTagHandler(null, cdkContext))
            .toThrowError(new Error('tag service is required'))

    })

    it('should try to tag with missing env', () => {

        expect(() => UOneTagHandler({}, null))
            .toThrowError(new Error('cdkContext parameter is required'))

    })

})
