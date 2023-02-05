const AWS = require("aws-sdk");
const { GameConstants } = require("./game-constants.js");
const { GameConfiguration } = require("../game-configuration.js");
AWS.config.logger = console;
/**
 * GameSearchService shared between all read and write paths
 * it provides interface to AWS ES for games data
 * 
 * @returns 
 */
function GameSearchService() {
    const baseDomain = process.env.GAME_STATE_SEARCH_DOMAIN_ENDPOINT;
    const host = GameConstants.HTTPS_FS + baseDomain;
    const region = process.env.REGION;
    let index;

    return {
        setIndex: function (indexName) {
            index = indexName;
        },
        getIndex: function (indexName) {
            return index;
        },
        /**
         * @request request object of HttpRequest
         *
         * makes calls to AWS ES
         */
        makeReuqestToES: async function (request) {
            var credentials = new AWS.EnvironmentCredentials("AWS");
            var signer = new AWS.Signers.V4(request, "es");
            signer.addAuthorization(credentials, new Date());
            var client = new AWS.HttpClient();
            return new Promise(function (resolve, reject) {
                client.handleRequest(
                    request,
                    null,
                    function (response) {
                        console.log(
                            response.statusCode + " " + response.statusMessage
                        );
                        var responseBody = "";
                        response.on("data", function (chunk) {
                            responseBody += chunk;
                        });
                        response.on("end", function (chunk) {
                            console.log(
                                "Response body Index Document: ",
                                responseBody
                            );
                            resolve(responseBody);
                        });
                    },
                    function (error) {
                        console.log("Error: " + error);
                        resolve(error);
                    }
                );
            });
        },
        /**
         * {
         *  "gameId",
         *  ...UOneGameObject // check confluence for UDMs
         * }
         *
         * document could any type of json document to be indexed
         * this indexes new document in the elastic search
         */
        indexDocumentSelfFormatedDocument: async function (document, id) {
            const type = index.toLowerCase();
            var request = new AWS.HttpRequest(host, region);
            document["indexed_at"] = new Date();
            console.log("document indexer", document);
            request.method = GameConstants.HTTP_METHOD.PUT;
            request.path += `${index}/${type}/${id}`;
            request.body = JSON.stringify(document);
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            try {
                await this.makeReuqestToES(request);
            } catch (ex) {
                console.log("error indexing ", ex);
            }
        },
        /**
         * 
         * {
         *  "gameId",
         *  ...UOneGameObject // check confluence for UDMs
         * }
         *
         *update game document by ID
         */
        updateSelfFormatedDocument: async function (document, id) {
            const type = index.toLowerCase();
            var request = new AWS.HttpRequest(host, region);
            document["indexed_at"] = new Date();
            request.method = GameConstants.HTTP_METHOD.POST;
            request.path += `${index}/${type}/${id}/_update`;
            request.body = JSON.stringify(document);
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            return await this.makeReuqestToES(request);
        },
        /**
         * @id it's uuid of the document to be removed
         *
         * this removes document from the elastic search
         */
        removeDocumentFromIndex: async function (id) {
            const type = index.toLowerCase();
            var request = new AWS.HttpRequest(host, region);
            request.method = GameConstants.HTTP_METHOD.DELETE;
            request.path += `${index}/${type}/${id}`;
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            return await this.makeReuqestToES(request);
        },
        /**
         * @document document to be indexed (same document as provided in indexDocumentByDynamoDBRecord)
         * @id it's uuid of the document to be removed
         *
         * document could any type of json document to be indexed
         * this indexesnew document in the elastic search
         */
        loadDocumentByDocumentId: async function (id) {
            let body = {
                query: {
                    bool: {
                        should: [
                            {
                                term: {
                                    "id.keyword": {
                                        value: id,
                                        boost: 1.0,
                                    },
                                },
                            },
                        ],
                    },
                },
            };
            var request = new AWS.HttpRequest(host, region);
            request.method = GameConstants.HTTP_METHOD.POST;
            request.path += `${index}/_search`;
            request.body = JSON.stringify(body);
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            let document = await this.makeReuqestToES(request);
            return document;
        },
        /**
         *
         * @param {string} term value to be searched in the given column
         * @param {string} column column name i.e. user_id, clientId
         * @param {number} page page number
         * @param {number} size size of page (number of results per page)
         * @returns array of found games (documents)
         */
        searchGamesByColumnAndTerm: async function (
            term,
            column,
            page = 0,
            size = 25
        ) {
            let from = page * size;
            let body = {
                size: size,
                from: from,
                query: {
                    query_string: {
                        query: term,
                        default_field: column,
                    },
                },
                sort: [{ indexed_at: { order: "desc" } }],
            };
            console.log("body ", JSON.stringify(body));
            var request = new AWS.HttpRequest(host, region);
            request.method = GameConstants.HTTP_METHOD.POST;
            request.path += `${index}/_search`;
            request.body = JSON.stringify(body);
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            let documents = JSON.parse(await this.makeReuqestToES(request));
            let games = { games: [] };
            if (documents.hits && documents.hits.hits) {
                games["games"] = documents.hits.hits;
            }
            return games;
        },
        /**
         *
         * @param {string} term value to be searched in the given column
         * @param {string} column column name i.e. user_id, clientId
         * @param {number} page page number
         * @param {number} size size of page (number of results per page)
         * @returns array of found games (documents)
         */
        findGameStateByUserIdAndGameId: async function (
            gameId,
            userId,
            page = 0,
            size = 25
        ) {
            let from = page * size;
            let body = {
                size: size,
                from: from,
                query: {
                    bool: {
                        must: [
                            {
                                match: {
                                    user_id: userId,
                                },
                            },
                            {
                                match: {
                                    game_id: gameId,
                                },
                            },
                        ],
                    },
                },
                sort: [{ indexed_at: { order: "desc" } }],
            };

            console.log("body ", JSON.stringify(body));
            var request = new AWS.HttpRequest(host, region);
            request.method = GameConstants.HTTP_METHOD.POST;
            request.path += `${index}/_search`;
            request.body = JSON.stringify(body);
            request.headers["host"] = baseDomain;
            request.headers["Content-Type"] = GameConstants.CONTENT_JSON;
            request.headers["Content-Length"] = Buffer.byteLength(request.body);
            let documents = JSON.parse(await this.makeReuqestToES(request));
            let games = { games: [] };
            if (documents.hits && documents.hits.hits) {
                games["games"] = documents.hits.hits;
            }
            return games;
        },
    };
}

module.exports = { GameSearchService };
