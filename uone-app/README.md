# HeyDayNow - APP (CRA)

- https://bitbucket.org/heydaynow/workspace/projects/HDNAPP

- https://bitbucket.org/heydaynow/app/src/development

## One time Only

```
mkdir -p ~/Sites/sls/heydaynow/uone-app
```

### Now is time to clone api repo

HTTPS

```
cd ~/Sites/sls/heydaynow/uone-app

git clone https://heydaynow@bitbucket.org/heydaynow/app.git .
```

SSH

```
cd ~/Sites/sls/heydaynow/uone-app

git clone git@bitbucket.org:heydaynow/app.git .
```

## code src/config.ts

You can get the info for this file from the `uone-api/api` repo, running the following command:

```
npm run info-dev | egrep '(ServiceEndpoint|CognitoUserPool|CognitoUserPoolClient|CognitoIdentityPool)'

# RESULT SHOULD LOOK LIKE:

CognitoUserPoolClient: 529a6nvkmrd8djil91luiegxvq
CognitoUserPool: us-west-2_qRgptqeE7
CognitoIdentityPool: us-west-2:d3e259ca-65c6-5b33-b9d8-a54c49811b34
ServiceEndpoint: https://51toh8g7f6.execute-api.us-west-2.amazonaws.com/dev
```

Note: Please remove `https://` and `/dev` sub-strings from `ServiceEndpoint` line, then replace `const APIG` value below with the URL from value.

Now is time to update your `src/config.ts` the file:

```
/**
 * sls info --aws-profile YOUR_PROFILE -vv
 *
 * https://serverless-stack.com/chapters/configure-aws-amplify.html
 */

const REGION = "us-west-2";

const APIG = "51toh8g7f6.execute-api.us-west-2.amazonaws.com";

const amplifyConfig = {
  apiGateway: {
    REGION,
    // ServiceEndpoint
    HOST: APIG,
    URL: `https://${APIG}/dev`,
  },
  cognito: {
    REGION,
    // CognitoUserPool
    USER_POOL_ID: "us-west-2_qRgptqeE7",
    // CognitoUserPoolClient
    APP_CLIENT_ID: "529a6nvkmrd8djil91luiegxvq",
    // CognitoIdentityPool
    IDENTITY_POOL_ID: "us-west-2:d3e259ca-65c6-5b33-b9d8-a54c49811b34",
  },
};

export default amplifyConfig;
```

## Branch out from the `development` branch

- https://bitbucket.org/heydaynow/app/src/development

```
git checkout development

git pull

nvm install && nvm use

# BRANCHING MODEL:

git checkout -b feature/short-description-HEYD-333
git checkout -b bugfix/short-description-HEYD-333
git checkout -b hotfix/short-description-HEYD-333
git checkout -b release/short-description-HEYD-333

# Happy Hacking Ninja!!!

code .
```

Note: Where `HEYD-333` must match the User Story ID from the JIRA Board:

- https://heydaynow.atlassian.net/secure/RapidBoard.jspa?projectKey=HEYD&rapidView=1

## Deploy and Remove (Tech Lead Job)

```
# Deploy Cloudformation Stack
npm run deploy-dev

# Info about the Cloudformation Stack
npm run info-dev

# Remove Cloudformation Stack
npm run remove-dev
```

### Use the new `devHeyDayNow` profile (Quick dirty hack)

If you are planning to use the new `devHeyDayNow` profile with `npm run deploy-dev`, `npm run info-dev` or `npm run remove-dev`, you must complete the following steps first:

```
cat ~/.aws/credentials
```

Now copy your current sand-boxed `key` and `secret` credentials and use them to create a new profile named `devHeyDayNow`:

```
sls config credentials --provider aws \
 --profile devHeyDayNow \
 --key YOUR_CURRENT_SAND_BOXED_IAM_USER_ACCESS_KEY_ID \
 --secret YOUR_CURRENT_SAND_BOXED_IAM_USER_SECRET_ACCESS_KEY
```

in order to to make this changes work, you must save and re-source or reload your shell console please.

#
