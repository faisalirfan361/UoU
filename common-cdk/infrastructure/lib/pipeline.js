const cdk = require('aws-cdk-lib');
const { CodePipeline, CodePipelineSource, ShellStep } = require('aws-cdk-lib/pipelines');
const { UOneTagHandler } = require('../../src/uone-tag-handler');
/**
 * This class will provide Pipeline for each micro's infra
 * You should implement infra, read and route stacks using this as parent
 */
class UOneCommonCDKPipeline extends cdk.Stack {
    envName;
    repo;
    branchName;
    uoneTagHandler;
    /**
     * initilizes the stack and creates tags
     * 
     * @param HDNStackScope scope 
     * @param String id stack id
     * @param String repo must provide repo name
     * @param String envName environment name where it should deploy
     * @param Strang branchName (must be dev, qa or prod)
     * @param HDNStackProps props 
     */
    constructor(scope, id, repo, envName, branchName, props) {
        super(scope, id, props);
        this.envName = envName;
        this.repo = repo;
        this.branchName = branchName;

        this.uoneTagHandler = UOneTagHandler(cdk.Tags, {envName: "shared-services"});
    }
    
    /**
     * provide base pipeline which can be used to append your stacks
     * you must implement self mutation for each stage of stack in the pipeline
     * 
     * @returns HDNPipeline
     */
    getPipeLine(){
        const pipeline = new CodePipeline(this, `UOne-Common-CDK-${this.envName}`, {
            pipelineName: `uone-common-cdk-${this.envName}`,
            selfMutation: true,
            crossAccountKeys: true,
            synth: new ShellStep('Synth', {
                input: CodePipelineSource.codeCommit(this.repo, this.branchName),
                commands: [
                    'cd infrastructure',
                    'npm i',
                    'npm run build',
                    'npm i -g cdk',
                    'cdk synth'
                ],
                primaryOutputDirectory: 'infrastructure/cdk.out',
            })
        });
        /**
         * Since these tages are shared accross the HDN so adding these here
         * it should auto apply in all resources inside your stacks
         */
        this.uoneTagHandler.tag(pipeline,"PROJECT","CommonCDK");
        this.uoneTagHandler.tag(pipeline,"ENVIRONMENT","SharedServices");
        this.uoneTagHandler.tag(pipeline,"FUNCTIONALITY","SDLC");
        this.uoneTagHandler.tag(pipeline,"OWNER","Eng");

        return pipeline;
    }
}
module.exports = { UOneCommonCDKPipeline }
