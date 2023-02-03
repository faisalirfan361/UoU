import os, json
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.expressions.function_expression import FunctionExpression
from uone_data_scraping.expressions.value_expression import ValueExpression


def test_function_expression_can_evaluate_with_no_params():
	data = {'thing': 'stuff', 'something': 'else'}
	state = {'job': {}, 'event': None,'data': data}
	exp = FunctionExpression(state, 'print_it', [])
	# res_state, result = exp.interpret()
	result = exp.interpret()
	# assert res_state == state
	assert result == None


def test_function_expression_can_evaluate_with_params():
	data = {'thing': 'stuff', 'something': 'else'}
	state = {'job': {}, 'event': None,'data': data}
	obj_arg = ValueExpression(state, '$$data')
	pointer_arg = ValueExpression(state, 'thing')

	exp = FunctionExpression(state, 'get_it', [obj_arg, pointer_arg])
	# res_state, result = exp.interpret()
	result = exp.interpret()
	assert result == 'stuff'
