from typing import Dict, Any
from . import bases
from . import expressions


class FunctionExpression(bases.UOneNonterminalExpression):
	def __init__(self, state:Dict[str,Any], command:str, sub_tree:bases.UOneAbstractExpression) -> None:
		self.state = state
		self.command_name = command
		self.command = getattr(expressions, command)
		self.sub_tree = sub_tree


	def interpret(self):
		# params_mapping = self.command.__annotations__
		return self.command(self.state, *[expression.interpret() for expression in self.sub_tree])
