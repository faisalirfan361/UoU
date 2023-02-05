const {HdMiCommonCDKPipeline } = require("./pipeline");
const cdk = require('aws-cdk-lib');
const {Repository } = require('aws-cdk-lib/aws-codecommit');
const {PolicyStatement, Effect} = require('@aws-cdk/aws-iam');
const {HdMiCommonCDKPackageStage } = require('./hdmi-common-cdk');
const { CodeBuildStep } = require("aws-cdk-lib/pipelines");
const { HdmiTagHandler } = require("../../src/hdmi-tag-handler");
/**
 * This is CI/CD project, This must added to each micro to make sure all pipelines
 * implement datadog and hystrix you may want to use HDNRepoGenerator
 */
class HdMiCommonCDKProject extends cdk.Stack {
    /**
     * 
     * @param HDNStackScope scope 
     * @param Strig id (Should be stack id)
     * @param Array environments array of project environments to deploy in data dog
     */
    constructor(scope, id, environments) {
        super(scope, id, { env: environments.get('shared-services') });

        const hdmiTagHandler = HdmiTagHandler(cdk.Tags, {envName: "shared-services"});
        hdmiTagHandler.tagStack(this,"CommonCDK","CDN");

        // We must download common CDK accross the board in eaech pipeline
        const repository = Repository.fromRepositoryName(this, 'HdMi-Common-CDK-Repo-Reference', 'common-cdk');
        hdmiTagHandler.tag(repository,"PROJECT","CommonCDK");
        hdmiTagHandler.tag(repository,"ENVIRONMENT","SharedServices");
        hdmiTagHandler.tag(repository,"FUNCTIONALITY","SDLC");
        hdmiTagHandler.tag(repository,"OWNER","Eng");

        const hdmiPipelineSbxMicro = new HdMiCommonCDKPipeline(this, 'HdMi-Common-CDK-Pipeline', repository, 'sbx-micro', 'sbx-micro', {env: environments.get('shared-services')});
        const hdmiPipelineSbxMicroStage = hdmiPipelineSbxMicro.getPipeLine();
        const myStage = hdmiPipelineSbxMicroStage.addStage(new HdMiCommonCDKPackageStage(this, 'HdMi-Common-CDK-Package-Stage', repository, 'sbx-micro', { env: environments.get('sbx-micro') }));
        myStage.addPost(this.addPublish(myStage, 'sbx-micro',repository))
    }
    
    /**
     * Each project will have several different stages to be published 
     * this add a stage/build step in current project and returns HDNBuildStep
     * to add docker file or custom scripts to be executed
     * 
     * @param String myStage stage name
     * @param String branchName must be branch name which will be used (typically stage name)
     * @param String repository  (repository name to be checked out from codecommit)
     * @returns HDNBuildStep 
     */
    addPublish(myStage, branchName,repository) {
        // console.log(repository);
        return new CodeBuildStep('Publish',{
            commands :[
                `aws codeartifact login --tool npm --repository hdmi-npm --domain hdmi --domain-owner ${process.env.DOMAIN_OWNER}`,
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
}
module.exports = { HdMiCommonCDKProject }
