const {GameReadServiceHandlers} = require('../../../../src/read/handlers/read/game-read-service-handlers')
const {Optional, Try} = require('@othree.io/optional')

describe('GameReadServiceHandlers', () => {

    describe('getHandler', () => {

        const baseGame = {
            id: '1234',
            code: 'kpi-001',
            label: 'kpi',
            type: 'Number',
            ranges: [{
                label:'initial',
                min: 0,
                max: 40
            }],
            filter: 'Everyone',
            clientId: '4321',
            isFlipped: false
        }

        it('should get a single game', async () => {
            const gameReadService = {
                get: async (gameId) => {
                    expect(gameId).toEqual('1234')
                    return Optional(baseGame)
                }
            }
            const gameReadServiceHandlers = GameReadServiceHandlers(gameReadService)

            const lambdaEvent = {
                query: {
                    gameId: '1234'
                }
            }

            const game = await gameReadServiceHandlers.getHandler(lambdaEvent)

            expect(game).toStrictEqual(baseGame)
        })

        it('should try to get a single game and throw an error if it fails', async () => {
            const gameReadService = {
                get: async (gameId) => {
                    expect(gameId).toEqual('1234')
                    return Try(async () => {
                        throw new Error('Error!')
                    })
                }
            }

            const gameReadServiceHandlers = GameReadServiceHandlers(gameReadService)

            const lambdaEvent = {
                query: {
                    gameId: '1234'
                }
            }

            await expect(gameReadServiceHandlers.getHandler(lambdaEvent)).rejects.toThrowError(new Error('Error!'))
        })

    })

})
