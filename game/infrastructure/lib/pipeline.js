const { HdmiTagHandler } = require('@hdmi/common-cdk');
const cdk = require('aws-cdk-lib');
const { PolicyStatement, Effect } = require('aws-cdk-lib/aws-iam');
const { CodePipeline, CodePipelineSource, CodeBuildStep } = require('aws-cdk-lib/pipelines');

/**
 *  HdMiGamePipeline base stack's core pipline for Game read, Write and Scheduler Stacks
 */
class HdMiGamePipeline extends cdk.Stack {
    envName;
    repo;
    branchName;
    hdmiTagHandler;
    prettyEnvName;

    /**
     * 
     * @param HDNCOntext scope 
     * @param String id 
     * @param String repo 
     * @param String envName 
     * @param String branchName 
     * @param HDNProps props 
     */
    constructor(scope, id, repo, envName, branchName, props) {
        super(scope, id, props);
        this.envName = envName;
        this.repo = repo;
        this.prettyEnvName = envName.replace(/-/g, " ").replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, "");
        this.branchName = branchName;
        this.hdmiTagHandler = HdmiTagHandler(cdk.Tags, {envName: envName});
        this.stages = [];
        this.code = CodePipelineSource.codeCommit(this.repo, this.branchName);
        this.pipeline = this.getPipeLine();

    }

    /**
     * Pass stage name and it will add new stage to pipeline
     * 
     * @param String stage 
     */
    addStage(stage) {
        console.log("Adding stage to pipeline...");
        this.stages.push(stage);
        this.pipeline.addStage(stage);
    }

    /**
     * Add publish stage on pipeline
     * 
     * @param String myStage 
     * @param String branchName 
     * @param String repository 
     * @returns 
     */
    addPublish(myStage, branchName, repository) {
        // console.log(repository);
        return new CodeBuildStep("Publish", {
            commands: [
                // branchName == "master" ? 'npm version clean' : 'npm version patch',
                "aws codeartifact login --tool npm --repository hdmi-npm --domain hdmi --domain-owner 972576019456",
                "cd codebase",
                "npm publish",
            ], 
            rolePolicyStatements: [
                /**
                 * access to code artifact for shared account only 
                 * since the pipelines will only work in shared account
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: ['codeartifact:*'],
                    resources: ["*"]
                }),
                /**
                 * get auth token for shared_services account code commit
                 * so we can checkout 
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: ['codeartifact:GetAuthorizationToken',
                    ],
                    resources: [`arn:aws:codeartifact:us-west-2:${process.env.DOMAIN_ID}:repository/hdmi/hdmi-npm`]
                }),
                /**
                 * publish PublishPackageVersion access
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "sts:AssumeRole",
                        "codeartifact:PublishPackageVersion"
                    ],
                    resources: ["arn:aws:iam::*:role/cdk-readOnlyRole",
                                "arn:aws:iam::*:role/cdk-*-lookup-role-*"
                    ]
                }),
                /**
                 * iam access to load aws cdk for ResourceTag
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "sts:AssumeRole",
                    ],
                    resources: ["*"],
                    conditions:{
                        "StringEquals": {
                            "iam:ResourceTag/aws-cdk:bootstrap-role": "lookup"
                        }
                    }
                }),
                /**
                 * iam access to load aws cdk for _resource_tag
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "sts:AssumeRole",
                    ],
                    resources: ["*"],
                    conditions:{
                        "StringEquals": {
                            "iam:_resource_tag/aws-cdk:bootstrap-role": "lookup"
                        }
                    }
                }),
                /**
                 * we need sts bearer token to checkout from code commit
                 * and generate role with access to it
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: ["sts:GetServiceBearerToken"],
                    resources: ["*"]
                }),
                /**
                 * Access to publish any packages to codeartiact
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "codeartifact:PublishPackageVersion",
                        "codecommit:*"
                    ],
                    resources: [`arn:aws:codeartifact:us-west-2:${process.env.DOMAIN_ID}:domain/hdmi/*`]
                }),
                /**
                 * full access to code commit for shared account
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: ["codecommit:*"],
                    resources: ["*"]
                }),
                /** 
                 * We need this to make sure we have assume Role access for multiaccount
                 */
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: ["sts:AssumeRole"],
                    resources: ["*"]
                })
            ]
        });
    }

    /**
     * Create code pipeline and return it for further adidng stages 
     * @returns 
     */
    getPipeLine(){
        const pipeline = new CodePipeline(this, `HdMi-Game-Pipeline-${this.envName}`, {
            pipelineName: `hdmi-game-${this.envName}`,
            selfMutation: true,
            crossAccountKeys: true,
            synth: new CodeBuildStep('Synth', {
                input: this.code,
                /**
                 * base commands to executed at synth and build stage to make sure stack is valid
                 */
                commands: [
                    'cd codebase',
                    'aws codeartifact login --tool npm --repository hdmi-npm --domain hdmi --domain-owner 972576019456',
                    'npm i',
                    'cd ../infrastructure',
                    'npm i',
                    'npm run build',
                    'npm i -g cdk',
                    `cdk synth HdMi-Games-Project-${this.prettyEnvName}/HdMi-Games-Pipeline-${this.prettyEnvName}`
                ],
                primaryOutputDirectory: 'infrastructure/cdk.out', 
                rolePolicyStatements: [
                    /**
                     * access to code artifact for shared account only 
                     * since the pipelines will only work in shared account
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: ['codeartifact:*'],
                        resources: ["*"]
                    }),
                    /**
                     * get auth token for shared_services account code commit
                     * so we can checkout 
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: ['codeartifact:GetAuthorizationToken',
                        ],
                        resources: [`arn:aws:codeartifact:us-west-2:${process.env.DOMAIN_ID}:repository/hdmi/hdmi-npm`]
                    }),
                    /**
                     * publish PublishPackageVersion access
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: [
                            "sts:AssumeRole",
                            "codeartifact:PublishPackageVersion"
                        ],
                        resources: ["arn:aws:iam::*:role/cdk-readOnlyRole",
                                    "arn:aws:iam::*:role/cdk-*-lookup-role-*"
                        ]
                    }),
                    /**
                     * iam access to load aws cdk for ResourceTag
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: [
                            "sts:AssumeRole",
                        ],
                        resources: ["*"],
                        conditions:{
                            "StringEquals": {
                                "iam:ResourceTag/aws-cdk:bootstrap-role": "lookup"
                            }
                        }
                    }),
                    /**
                     * iam access to load aws cdk for _resource_tag
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: [
                            "sts:AssumeRole",
                        ],
                        resources: ["*"],
                        conditions:{
                            "StringEquals": {
                                "iam:_resource_tag/aws-cdk:bootstrap-role": "lookup"
                            }
                        }
                    }),
                    /**
                     * we need sts bearer token to checkout from code commit
                     * and generate role with access to it
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: ["sts:GetServiceBearerToken"],
                        resources: ["*"]
                    }),
                    /**
                     * Access to publish any packages to codeartiact
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: [
                            "codeartifact:PublishPackageVersion",
                            "codecommit:*"
                        ],
                        resources: [`arn:aws:codeartifact:us-west-2:${process.env.DOMAIN_ID}:domain/hdmi/*`]
                    }),
                    /**
                     * full access to code commit for shared account
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: ["codecommit:*"],
                        resources: ["*"]
                    }),
                    /** 
                     * We need this to make sure we have assume Role access for multiaccount
                     */
                    new PolicyStatement({
                        effect: Effect.ALLOW,
                        actions: ["sts:AssumeRole"],
                        resources: ["*"]
                    })
                ]
            })
        });

        /**
         * base stack tags
         */
        this.hdmiTagHandler.tag(pipeline,"PROJECT", "HdMiGames");
        this.hdmiTagHandler.tag(pipeline,"ENVIRONMENT", this.envName.replace(/-/g, " ").replace(/\w\S*/g, function(txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        }).replace(/ /g, ""));
        this.hdmiTagHandler.tag(pipeline,"FUNCTIONALITY", "SDLC");
        this.hdmiTagHandler.tag(pipeline,"OWNER", "Eng");
        
        return pipeline;
    }
}
module.exports = { HdMiGamePipeline }
