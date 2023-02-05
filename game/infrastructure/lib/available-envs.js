const AVAILABLE_ENVS = {
    DEV: 'dev',
    QA: 'QA',
    INTEGRATION: 'Integration',
    Prod: 'Prod',
    ScheduleTableIndexes: {
        "SearchByClient": "SearchByClient"
    },
    UserPerformanceTableIndexes: {
        "ByDepartmentAndUserId": "DepartmentIdAndUserId"
    },
    CDK_ENVS: {
        "sbx-micro": { account: '677179051929', region: 'us-west-2' },
        "dev": { account: '379516241584', region: 'us-west-2' }
    }
}
module.exports = {AVAILABLE_ENVS}