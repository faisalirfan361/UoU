# UOne Common-CDK #
This is a wrapper over AWS CDK, it will help us with all CICD/SLDC by providing bases classes and utlities which take
care of most of the basic settings.

## Contributors:
   1. M. Faisal Irfan EM/Architect
   2. Awais Ali
   3. Adam Phillipps

### What is this repository for? ###
Wrapper for AWS CDK, provides base classes and utlitlies which help you create stacks, stages and pipelines for your CI/CD. Auto creates tags, it's used in the UOneDataDog Service to create mappings and integrations. We also use this for adding reports and provide control to hystrix.

### How do I get set up? ###
You must reach out to Faisal or Adam for access token and permissions to the repo. finally:
npm install @uone/common-cdk 

it will download the package and you are all set to go. (make sure you AWS Vault is setup).

### Contribution guidelines ###
To add new feature:
1. Make sure approved by Faisal or Adam and your EM
2. You need to have a ticket in UOne-CommonCDK (on JIRA)
3. Apply your changes
4. Writing tests
5. Create PR
6. Make sure you have ran sed lint and all tests pass
7. once approved just merge and in approval step for pipeline request Faisal to approve it

### Who do I talk to? ###

* Faisal Irfan or Awais Ali (or if you want to wait for days try to reach out to Adam Phillipps)
