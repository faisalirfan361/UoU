console.info('test coverage percentage', process.env.CODE_COVERAGE_PERCENTAGE || 100)
module.exports = {
    collectCoverage: true,
    coverageDirectory: './coverage',
    coverageReporters: ['html', 'text', 'text-summary'],
    coverageThreshold: {
        global: {
            branches: Number(process.env.CODE_COVERAGE_PERCENTAGE) || 100,
            functions: Number(process.env.CODE_COVERAGE_PERCENTAGE) || 100,
            lines: Number(process.env.CODE_COVERAGE_PERCENTAGE) || 100,
            statements: Number(process.env.CODE_COVERAGE_PERCENTAGE) || 100
        }
    },
    preset: "jest-dynalite"
}
