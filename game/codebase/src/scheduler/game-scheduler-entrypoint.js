const AWS = require("aws-sdk");
const { GameConfiguration } = require("../game-configuration"); 
const {
    UserPerformanceService,
} = require("./services/user-performance-service"); 
const {
    UserPerformanceReadRepository,
} = require("./repositories/user-performance-read-repository");

const { UserPerformanceHandler } = require("./user-performance-handler");
const { CalculatorClient } =
    require("@uone/uone-calculator-clients").Calculator;

const documentClient = new AWS.DynamoDB.DocumentClient(); 
const userPerformanceReadRepository = UserPerformanceReadRepository(
    documentClient,
    GameConfiguration
); 

const userPerformanceService = UserPerformanceService(
    userPerformanceReadRepository
);

const userPerformanceHandler = UserPerformanceHandler(userPerformanceService);

/**
 * load all dependencies and provides entry for user performance service
 */
module.exports = {
    rest: {
        ...userPerformanceHandler,
    },
};
