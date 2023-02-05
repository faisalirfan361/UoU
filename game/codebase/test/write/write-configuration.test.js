describe('WriteConfiguration', () => {

    process.env.EVENT_TABLE = 'EventTable'
    process.env.EVENT_BUS_ARN = 'sns:event:bus'

    const {WriteConfiguration} = require('../../src/write/write-configuration')

    it('should return the configured event sourcing table', () => {
        expect(WriteConfiguration.EventTable).toEqual('EventTable')
    })

    it('should return the configured event bus ARN', ()=> {
        expect(WriteConfiguration.EventBusARN).toEqual('sns:event:bus')
    })

})
