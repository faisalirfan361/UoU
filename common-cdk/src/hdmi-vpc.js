const environment = require('./environment-configuration')
const ssm = require("aws-cdk-lib/aws-ssm");

function UOneVPC(scope, ec2) {
    return Object.freeze({
        fromAttributes: (name, vpcId, subnetIds, region = environment.getRegion()) => {
            if(!name) {
                throw new Error('name is required')
            }

            if(!vpcId) {
                throw new Error('vpcId is required')
            }

            return ec2.Vpc.fromVpcAttributes(scope, name, {
                vpcId: vpcId,
                availabilityZones: [region],
                privateSubnetIds: subnetIds
            })
        },
        /**
         * returns VPC and security groups from backbone
         * 
         * @param object cdk object
         * @param string backbonEnvName name of env
         * @param array name of subnets you want to get (by default application since all lambdas are in this group ) 
         * @param string resourceId optional if you want to pass resourceId for the VPC
         * @param string name of the region 
         * @returns { ...vpc, ...securityGroups }
         */
        getDefaultUOneConfigWithVPC(cdk, backbonEnvName, types=["application"], resourceId="vpc", region = environment.getRegion()) {
            const vpcId = ssm.StringParameter.valueFromLookup(
                scope,
                "HdN-VPC-ID"
            );
            const SUBNETS = {
                "application": [
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Application-a-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Application-b-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Application-c-Subnet-ID"
                    )
                ],
                "database": [
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Database-a-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Database-b-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Database-c-Subnet-ID"
                    )
                ],
                "ingress": [
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Ingress-a-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Ingress-b-Subnet-ID"
                    ),
                    ssm.StringParameter.valueFromLookup(
                        scope,
                        "Ingress-c-Subnet-ID"
                    )
                ]
            }
            let subnetIds = [], subnetObjs = [];
            for (let i=0; i < types.length; i++){
                subnetIds = [...subnetIds, ...SUBNETS[types[i]]]
            }
            console.log("subnetIds ", subnetIds)
            for (let j=0; j < subnetIds.length; j++) {
                subnetObjs.push(ec2.Subnet.fromSubnetId(scope, `${resourceId}-subnet-${j}`, subnetIds[j]));
            }

            const securityGroupId = ssm.StringParameter.valueFromLookup(
                scope,
                "PrivateResourcesSecurityGroupId"
            );
            const iSecurityGroup = ec2.SecurityGroup.fromSecurityGroupId(
                scope,
                `${resourceId}-SG`,
                securityGroupId
            );
            return {
                "vpc": ec2.Vpc.fromVpcAttributes(scope, `VPC-${backbonEnvName}-${resourceId}`, {
                    vpcId: vpcId,
                    availabilityZones: cdk.Fn.getAzs(),
                    privateSubnetIds: subnetIds
                }),
                "securityGroups": [iSecurityGroup],
                "vpcSubnets":{
                    subnets: subnetObjs
                } 
            }

        }
    })
}

module.exports = {UOneVPC}