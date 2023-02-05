const GameConfiguration = Object.freeze({
    GameTable: process.env.GAME_TABLE,
    GameIndex: process.env.GAME_INDEX,
    GameStateIndex: process.env.GAME_STATE_INDEX,
    UserPerformanceTable: process.env.USER_PERFORMANCE_TABLE,
    CalculateFunction: process.env.CALCULATOR_CALCULATE_FN,
    GetMetricByClientCodeAndMetricCode:
        process.env.GET_METRIC_BY_CLIENT_CODE_AND_METRIC_CODE,
    GetEntityListByTypeFN: process.env.GET_ENTITIES_BY_TYPE,
    GetAllEntitiesByTypeNClientNoCognitoFN:
        process.env.GET_ALL_ENTITIES_BY_TYPE_N_CLIENT_NO_COGNITO_FN,
    GetEntitiesByIdListFN: process.env.GET_ENTITIES_BY_ID_List_FN,
    GameIndexes: {
        SEARCH_BY_CLIENT_ID_ISDEUL: "SearchByClientWithDuels",
        SEARCH_BY_CLIENT_ID: "SearchByClient",
        SEARCH_BY_CLIENT_ID_WITH_CREATED_AT: "SearchByClientWithCreatedAt",
    },
    GameIndexIDColumns: {
        USER_ID: "profiles.users.user_id",
        CLIENT_ID: "clientId",
    },
    UserSearchType: {
        USER_ID: "USER_ID",
        CLIENT_ID: "CLIENT_ID",
    },
    GameFilters: {
        ALL: "",
        ACTIVE: "isComplete <> :true",
        COMPLETE: "isComplete = :true and attribute_exists(winnerProfile)",
        DRAW: "isComplete = :true and attribute_not_exists(winnerProfile)",
    },
});

module.exports = { GameConfiguration };
