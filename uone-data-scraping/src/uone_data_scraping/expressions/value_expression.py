from typing import Dict, Any
from . import bases


class ValueExpression(bases.UOneTerminalExpression):
	def __init__(self, state:Dict[str,Any], value:str) -> None:
		self.state = state
		self.value = value


	def interpret(self) -> Any:
		if isinstance(self.value, str) and self.value.strip('$$') in self.state.keys():
			return self.state[self.value.strip('$$')]
		else:
			return self.value
