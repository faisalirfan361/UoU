const {GameReadRestHandlers} = require('../../../../src/read/handlers/read/game-read-rest-handlers')
const {Optional} = require('@othree.io/optional')

describe('GameReadRestHandlers', () => {

    it('should get the game by id', async () => {

        const expectedGame = {
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
            isFlipped: false,
            status: true
        }

        const gameReadService = {
            get: async (gameId) => {
                expect(gameId).toStrictEqual('1234')
                return Optional(expectedGame)
            }
        }

        const gameRestHandlers = GameReadRestHandlers(gameReadService)

        const lambdaEvent = {
            pathParameters: {
                gameId: '1234'
            }
        }

        const response = await gameRestHandlers.getHandler(lambdaEvent)

        expect(response).toStrictEqual({
            statusCode: 200,
            body: JSON.stringify(expectedGame),
            headers: {'Access-Control-Allow-Origin': '*'}
        })
    })

    it('should try to get the game by id with missing id', async () => {

        const gameReadService = {}
        const gameRestHandlers = GameReadRestHandlers(gameReadService)

        const lambdaEvent = {
            pathParameters: {}
        }

        const response = await gameRestHandlers.getHandler(lambdaEvent)

        expect(response).toStrictEqual({
            statusCode: 400,
            body: JSON.stringify('id is required'),
            headers: {'Access-Control-Allow-Origin': '*'}
        })
    })

    it('should try to get the game by id and return 404 if not found', async () => {
        const gameReadService = {
            get: async (gameId) => {
                expect(gameId).toEqual('1234')
                return Optional()
            }
        }
        const gameRestHandlers = GameReadRestHandlers(gameReadService)

        const lambdaEvent = {
            pathParameters: {
                gameId: '1234'
            }
        }

        const response = await gameRestHandlers.getHandler(lambdaEvent)

        expect(response).toStrictEqual({
            statusCode: 404,
            body: JSON.stringify(undefined),
            headers: {'Access-Control-Allow-Origin': '*'}
        })
    })

})
