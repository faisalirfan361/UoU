const cdk = require("aws-cdk-lib");
const cxschema = require('aws-cdk-lib/cloud-assembly-schema');
/**
 * Utility for ssmString Params for AWS
 * we must use this for all stack out puts, 
 * do not use the stack out params as they create circular dependencies
 * 
 * @param UOneStackScope scope 
 * @param String parameterName 
 * @param String dummyValue 
 * @returns 
 */
export function ssmStringParameterLookupWithDummyValue(
  scope,
  parameterName,
  dummyValue
){
  return cdk.ContextProvider.getValue(scope, {
    provider: cxschema.ContextProvider.SSM_PARAMETER_PROVIDER,
    props: {
      parameterName,
    },
    dummyValue: dummyValue,
  }).value;
}