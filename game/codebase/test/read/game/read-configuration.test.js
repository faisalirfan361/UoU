describe('ReadConfiguration', () => {

    process.env.READ_TABLE = 'GameReadTable'

    const {ReadConfiguration} = require('../../../src/read/read-configuration')

    it('should return the configured game read table', () => {
        expect(ReadConfiguration.ReadTable).toEqual('GameReadTable')
    })

})
