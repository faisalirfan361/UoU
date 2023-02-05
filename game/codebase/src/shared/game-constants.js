/**
 * Add all your game constants here
 */

const GameConstants = Object.freeze({
    HTTPS_FS: "https://",
    HTTP_METHOD: {
        'PUT': 'PUT',
        'POST': 'POST',
        'DELETE':'DELETE',
        'GET': 'GET'
    },
    CONTENT_JSON: 'application/json',
    GAME_SCHEDULE_TYPES: {
        'ONCE': 'ONCE',
        'DAILY': 'DAILY',
        'WEEKLY': 'WEEKLY',
        'MONTHLY': 'MONTHLY'
    },
    GRAPH_RELATIONS: {
        "HAS_GAME": "has-game",
        "MEASURED_BY": "measured-by",
        "IN_COMPETITION_WITH": "in-competition-with",
        "IN_COMPETITION_WITH_GAME_GROUP": "game_contains"
    }
})

module.exports = { GameConstants };