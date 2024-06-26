const { UOneGamePipeline } = require("./pipeline");
const cdk = require("aws-cdk-lib");
const { Repository } = require("aws-cdk-lib/aws-codecommit");
const { PolicyStatement, Effect } = require("@aws-cdk/aws-iam");
const { UOneGamePackageStage } = require("./uone-game-stage");
const { CodeBuildStep } = require("aws-cdk-lib/pipelines");
const { UOneCdkContext, UOneTagHandler } = require("@uone/common-cdk");
const { GameReadDefinitionStage } = require("./game-read-definition");
const { GameWriteDefinitionStage } = require("./game-write-definition");
const { GameSchedulerDefinitionStage } = require("./game-scheduler-definition");
const { AVAILABLE_ENVS } = require("./available-envs");

/**
 * UOneGameProject for add a project for the repo in codecommit pipelines
 */
class UOneGameProject extends cdk.Stack {
    constructor(scope, id, envName, environments) {
        super(scope, id, { env: environments.get("shared-services") });

        const prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");
        const cdkContext = UOneCdkContext(scope, envName);
        const uoneTagHandler = UOneTagHandler(cdk.Tags, {
            envName: "shared-services",
        });

        // Load repo from CodeCommit
        const repository = Repository.fromRepositoryName(
            this,
            `UOne-Games-Repo-Reference-${envName}`,
            "game"
        );
        uoneTagHandler.tag(repository, "PROJECT", "UOneGames");
        uoneTagHandler.tag(repository, "ENVIRONMENT", "SharedServices");
        uoneTagHandler.tag(repository, "FUNCTIONALITY", "SDLC");
        uoneTagHandler.tag(repository, "OWNER", "Eng");

        /**
         * create base pipeline
         */
        const pipeline = new UOneGamePipeline(
            this,
            `UOne-Games-Pipeline-${prettyEnvName}`,
            repository,
            envName,
            environments.get(envName).branch,
            { env: environments.get("shared-services") }
        );
        
        /**
         * Read stack pros for CF
         */
        const readStackProps = {
            tags: {
                PROJECT: "UOneGames",
                ENVIRONMENT: this.prettyEnvName,
                OWNER: "Eng",
                backboneEnv: environments.get(envName).backboneEnv,
            },
            env: envName,
            cdkEnv: environments.get(envName),
        }
        
        /**
         * Write stack pros for CF
         */
        const writeStackProps = {
            tags: {
                PROJECT: "UOneGames",
                ENVIRONMENT: prettyEnvName,
                OWNER: "Eng",
                backboneEnv: environments.get(envName).backboneEnv,
            },
            env: envName,
            cdkEnv: environments.get(envName),
        };
        
        /**
         * Add stage for Read Definition
         */
        pipeline.addStage(new GameReadDefinitionStage(
            this,
            `UOne-Games-Read-Stage-${prettyEnvName}`, 
            cdkContext,
            envName,
            readStackProps,
            { env: environments.get(envName) }
        ));
        /**
         * Add stage for Write Definition
         */
        pipeline.addStage(new GameWriteDefinitionStage(
            this,
            `UOne-Games-Write-Stage-${prettyEnvName}`,
            cdkContext,
            envName,
            writeStackProps,
            { env: environments.get(envName) }
        ));
        /**
         * Add stage for Scheduler Definition
         */
        pipeline.addStage(new GameSchedulerDefinitionStage(
            this,
            `UOne-Games-Scheduler-Stage-${prettyEnvName}`,
            cdkContext,
            envName,
            writeStackProps,
            { env: environments.get(envName) }
        ));
    }
}

module.exports = { UOneGameProject };
