module.exports = {
  parser: "@typescript-eslint/parser",
  parserOptions: {
    project: "./tsconfig.json",
    rules: {
      "react-hooks/exhaustive-deps": 0,
    },
  },
  plugins: ["@typescript-eslint"],
};
