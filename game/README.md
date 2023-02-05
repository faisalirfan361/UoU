
# games

## AWS Stack Tags

|Name            |Value                                  |
|----------------|--------------------------             |
|ENV             |DEV - QA - PROD                        |
|PROJECT         |UOne-Games-Write--{ENV}             |
|PROJECT         |UOne-Games-Read-{ENV}               |



## Deploy
To deploy a new environment, you have to define it in the `bin/main.json` file and then run the deploy
manually, from your laptop.  the deploy command will be something like `cdk deploy UOne-Games-Project-Dev`
