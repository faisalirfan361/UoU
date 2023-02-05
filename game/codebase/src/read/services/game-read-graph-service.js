/**
 * ReadQuestGraphService is read tunnel to AWS Npetune (Quest)
 * 
 * @param QuestServiceClient questServiceClient 
 * @returns 
 */
function ReadQuestGraphService(questServiceClient) {
    /**
     * it should Communicate with QuestClient to get you maps and hierachy of games 
     * graphDB, AWS Neptune.
     * @param Game[] result 
     * @param String queryResult 
     * @param String label 
     * @param Bool id 
     */
    const getEntitiesByLabel = (result, queryResult, label, id=true) => {

        if('type' in queryResult){
            if (
                ((Array.isArray(queryResult.type) && queryResult.type.includes(label)) || queryResult.type === label) &&
                queryResult.id
            ) {
                if(id){
                    if (Array.isArray(queryResult.id)) {
                        queryResult.id.map((i) => {
                            if (!result.includes(i)) {
                                result.push(i);
                            }
                        });
                    } else {
                        if (!result.includes(queryResult.id)) {
                            result.push(queryResult.id);
                        }
                    }
                }
                else{
                    let found = -1;
                    let resultIds = [];
                    result.map((i) => {
                        Array.isArray(i.id) ? resultIds.push(...i.id) :resultIds.push(i.id);
                    });
                    if (Array.isArray(queryResult.id)) {
                        found = queryResult.id.some(item => resultIds.includes(item))
                    }else{
                        found = resultIds.includes(queryResult.id);
                    }
                    if (!found) {
                        result.push(queryResult);
                    }
                }
                
            }
          }

        queryResult.items.map(item =>{
            getEntitiesByLabel(result, item, label, id)
        })
    }
    return {
        /**
         * pass script, values, it returns the list of entities
         * @param str entityId 
         * @returns 
         */
        getResultByScript: async (script, vals) => {
            console.log("getResultByScript script ", script, vals);
            let queryResult = await questServiceClient.executeScript(script, vals);
            console.log("getResultByScript queryResult", queryResult.get());
            let result = queryResult.get();
            return result;
        },
        /**
         * pass script, values, it returns the list of entities
         * @param str entityId 
         * @returns 
         */
         getResultObjetByScript: async (script, vals, label) => {
            let queryResult = await questServiceClient.executeScript(script, vals);
            console.log("getResultObjetByScript queryResult", queryResult.get());
            let result = [];
            queryResult = queryResult.get();
            Object.keys(queryResult).map((key)=>{
                getEntitiesByLabel(result, queryResult[key], label, false);
            })
            return result;
        }

    }
}

module.exports = {ReadQuestGraphService}
