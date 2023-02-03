from typing import Any, Dict, List
from ..expressions import expressions
from ..expressions.function_expression import FunctionExpression
from ..expressions.value_expression import ValueExpression
from ..expressions.bases import UOneAbstractExpression


class ProcessingStack():
	def __init__(self, state, *instructions:Dict[str,Any]):
		assert {'job', 'event', 'data'}.issubset(set(state))

		self.state = state
		self.instructions = instructions
		self.stack = self._build_stack(*instructions)
		self.tree_root = self.stack[0]


	def interpret(self):
		# return self.tree_root.interpret()
		results = [expression.interpret() for expression in self.stack]
		return results[0] if len(results) == 1 else results


	def _build_stack(self, *instructions:Dict[str,Any]) -> List[UOneAbstractExpression]:
		_internal = []

		for instruction in instructions:
			_internal.append(self._node_builder(instruction))

		return _internal


	def _node_builder(self, command:Any) -> UOneAbstractExpression:
		print(f"Building command node from: {command}")

		if isinstance(command, dict) and list(command.keys())[0] in self.all_func_names():
			comm_name = list(command.keys())[0]
			comm_args = list(command.values())[0]
			node = FunctionExpression(self.state, comm_name, self._build_stack(*comm_args))
		else:
			node = ValueExpression(self.state, command)

		return node


	def all_func_names(self) -> List[str]:
		builtins_types_etc = [
			'Any', 'Callable', 'Dict', 'List', 'Tuple', '__builtins__', '__cached__',
			'__doc__', '__file__', '__loader__', '__name__', '__package__', '__spec__'
		]
		return list(set(dir(expressions)) - set(builtins_types_etc))
