import * as yup from "yup";
import { getExpressionTree } from "./expresionTreeParser";
import { MathNode, string } from "mathjs";

export const defaultValuesCreateIndicator = {
  indicator_name: "",
  formula: "",
};

const checkBrackets = (str: string) => {
  var lb,
    rb,
    li,
    ri,
    i = 0,
    brkts = ["(", ")", "{", "}", "[", "]"];
  while (((lb = brkts[i++]), (rb = brkts[i++]))) {
    li = ri = 0;
    while ((li = str.indexOf(lb, li) + 1)) {
      if ((ri = str.indexOf(rb, ri) + 1) < li) {
        return false;
      }
    }
    if (str.indexOf(rb, ri) + 1) {
      return false;
    }
  }
  return true;
};

const checkKpis = (node: MathNode, kpisCount: number): number => {
  let count = kpisCount;
  let right = 0;
  let left = 0;

  switch (node.type) {
    case "FunctionNode":
      if (!node.args) return 0;

      if (node.args.length == 2) {
        right = checkKpis(node.args[1], count);
        left = checkKpis(node.args[0], count);
        return right + left;
      } else {
        count = checkKpis(node.args[0], count);
        return count;
      }

    case "ParenthesisNode":
      if (!node.content) return 0;
      if (!node.content.args) return 0;

      right = checkKpis(node.content?.args[1], count);
      left = checkKpis(node.content?.args[0], count);
      return left + right;

    case "OperatorNode":
      if (!node.args) return 0;

      if (node.args.length == 1) {
        right = checkKpis(node.args[0], count);
        return count + right;
      } else {
        right = checkKpis(node.args[1], count);
        left = checkKpis(node.args[0], count);
        return left + right;
      }
    case "ConstantNode":
      return 0;
    case "SymbolNode":
      return 1;
    default:
      return 0;
  }
};

const usesKPIs = (str: string): boolean => {
  const expressionTree: MathNode | null = getExpressionTree(`${str}`);
  if (!expressionTree) return false;

  const kpiCount = checkKpis(expressionTree, 0);
  return kpiCount >= 1;
};

export const validationSchemaCreateIndicator = yup.object().shape({
  indicator_name: yup
    .string()
    .required("Indicator name is required")
    .nullable(),
  formula: yup
    .string()
    .required("Formula is required")
    .test(
      "test-brackets",
      "The formula has missing brackets",
      (value, context) => checkBrackets(`${value}`)
    )
    .test("test-formula", "The formula is not valid", (value, context) => {
      const expressionTree = getExpressionTree(`${value}`);
      return expressionTree ? true : false;
    })
    .test(
      "test-formula",
      "You need to use at least 1 Kpi",
      (value, context) => {
        return usesKPIs(`${value}`);
      }
    )
    .nullable(),
});
