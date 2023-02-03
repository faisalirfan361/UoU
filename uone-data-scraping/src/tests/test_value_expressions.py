import os, json
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.expressions.value_expression import ValueExpression


def test_value_expression_can_use_state_with_dollar_dollar_notation():
	data = {'thing': 'stuff', 'something': 'else'}
	state = {'job': {}, 'event': None,'data': data}
	exp = ValueExpression(state, '$$data')
	result = exp.interpret()
	assert result == data


def test_value_expression_can_use_any_kind_of_object():
	data = {'thing': 'stuff', 'something': 'else'}
	state = {'job': {}, 'event': None,'data': data}
	exp = ValueExpression(state, 5)
	result = exp.interpret()
	assert result == 5


# def test_build_stack_can_build_tree_with_nested_functions():
# 	comm = {
# 		'get_it': [
# 			'$$data',
# 			{'get_it': ['$$event', 'i_am_nested']}
# 		]
# 	}

# 	state = {'job': comm, 'event': {'i_am_nested':'something'},'data': {'thing': 'stuff', 'something': 'else'}}
# 	stack = ProcessingStack(state, comm)
# 	result = stack.run()
# 	assert result == 'else'
