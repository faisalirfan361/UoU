const {HdmiTagHandler} = require('../src/hdmi-tag-handler')

describe('HdmiTagHandler', () => {

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

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        hdmiTagHandler.tag(testScope, 'testKey', 'testValue')

        expect(tagService.of).toBeCalledTimes(1)
        expect(tagService.add).toBeCalledTimes(1)
    })

    it('should try to add single tag with missing scope', () => {

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        expect(() => hdmiTagHandler.tag(null, 'tagKey', 'testValue'))
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

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        expect(() => hdmiTagHandler.tag(testScope, '', 'testValue'))
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

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        expect(() => hdmiTagHandler.tag(testScope, 'tagKey', ''))
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

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        hdmiTagHandler.tagStack(testScope, 'Hdmi-Testing')

        expect(tagService.of).toBeCalledTimes(3)
        expect(tagService.add).toBeCalledTimes(3)
    })

    it('should try to tag a stack with missing scope', () => {

        const testScope = {}

        const tagService = {
            of: jest.fn().mockReturnThis(),
            add: jest.fn().mockReturnThis()
        }

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        expect(() => hdmiTagHandler.tagStack(null, 'Hdmi-Test'))
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

        const hdmiTagHandler = HdmiTagHandler(tagService, cdkContext)

        expect(() => hdmiTagHandler.tagStack(testScope, ''))
            .toThrowError(new Error('stackName parameter is required'))

        expect(tagService.of).toBeCalledTimes(0)
        expect(tagService.add).toBeCalledTimes(0)
    })

    it('should try to tag with missing tag service', () => {

        expect(() => HdmiTagHandler(null, cdkContext))
            .toThrowError(new Error('tag service is required'))

    })

    it('should try to tag with missing env', () => {

        expect(() => HdmiTagHandler({}, null))
            .toThrowError(new Error('cdkContext parameter is required'))

    })

})
