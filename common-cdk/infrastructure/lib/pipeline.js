const cdk = require('aws-cdk-lib');
const { CodePipeline, CodePipelineSource, ShellStep } = require('aws-cdk-lib/pipelines');
const { HdmiTagHandler } = require('../../src/hdmi-tag-handler');
/**
 * This class will provide Pipeline for each micro's infra
 * You should implement infra, read and route stacks using this as parent
 */
class HdMiCommonCDKPipeline extends cdk.Stack {
    envName;
    repo;
    branchName;
    hdmiTagHandler;
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

        this.hdmiTagHandler = HdmiTagHandler(cdk.Tags, {envName: "shared-services"});
    }
    
    /**
     * provide base pipeline which can be used to append your stacks
     * you must implement self mutation for each stage of stack in the pipeline
     * 
     * @returns HDNPipeline
     */
    getPipeLine(){
        const pipeline = new CodePipeline(this, `HdMi-Common-CDK-${this.envName}`, {
            pipelineName: `hdmi-common-cdk-${this.envName}`,
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
        this.hdmiTagHandler.tag(pipeline,"PROJECT","CommonCDK");
        this.hdmiTagHandler.tag(pipeline,"ENVIRONMENT","SharedServices");
        this.hdmiTagHandler.tag(pipeline,"FUNCTIONALITY","SDLC");
        this.hdmiTagHandler.tag(pipeline,"OWNER","Eng");

        return pipeline;
    }
}
module.exports = { HdMiCommonCDKPipeline }
