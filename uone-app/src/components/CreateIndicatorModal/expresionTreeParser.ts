import { create, all, MathNode } from "mathjs";

const buildNode = (node: any) => {
  switch (node.fn.name) {
    case "Sum":
      return { sum: node.args[0].name };
    case "Count":
      return { count: node.args[0].name };
    case "Avg":
      return { avg: node.args[0].name };
  }
  return {};
};

const buildNodeExpression = (node: any, left: any, right: any) => {
  switch (node.fn) {
    case "divide":
      return { divide: [left, right] };
    case "multiply":
      return { multiply: [left, right] };
    case "add":
      return { add: [left, right] };
    case "subtract":
      return { subtract: [left, right] };
    case "unaryMinus":
      return { multiply: [-1, left] };
  }
  return {};
};

const build = (node: any): any => {
  switch (node.type) {
    case "FunctionNode":
      if (node.args.length == 2) {
        const rightF = build(node.args[1]);
        const leftF = build(node.args[0]);
        return buildNodeExpression(node, leftF, rightF);
      } else {
        return buildNode(node);
      }

    case "ParenthesisNode":
      const rightP = build(node.content.args[1]);
      const leftP = build(node.content.args[0]);
      return buildNodeExpression(node.content, leftP, rightP);
    case "OperatorNode":
      if (node.args.length == 1) {
        const val = build(node.args[0]);
        return buildNodeExpression(node, val, {});
      } else {
        const rightO = build(node.args[1]);
        const leftO = build(node.args[0]);
        return buildNodeExpression(node, leftO, rightO);
      }

    case "ConstantNode":
      return { constant: node.value };
    case "SymbolNode":
      return { sum: node.name };
    default:
      return { node };
  }
};

export const parseNodeTree = (node: any) => {
  return build(node);
};

export const getExpressionTree = (formula: string): MathNode | null => {
  const math = create(all, {});
  const cleanFormula = formula.trim().replace(/{/g, "").replace(/}/g, "");
  try {
    const node: MathNode | null = math?.parse ? math.parse(cleanFormula) : null;
    return node;
  } catch (e) {
    console.log(e);
    return null;
  }
};
