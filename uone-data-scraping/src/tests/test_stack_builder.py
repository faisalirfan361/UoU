import os, json
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.processing.stack_builder import ProcessingStack


def test_build_stack_can_build_tree_with_only_a_value():
	comm = {'get_it': ['$$data', 'something']}
	state = {'job': comm, 'event': None,'data': {'thing': 'stuff', 'something': 'else'}}
	stack = ProcessingStack(state, comm)
	# res_state, result = stack.run()
	result = stack.interpret()
	assert result == 'else'


def test_build_stack_can_build_tree_with_nested_functions():
	comm = {
		'get_it': [
			'$$data',
			{'get_it': ['$$event', 'i_am_nested']}
		]
	}

	state = {'job': comm, 'event': {'i_am_nested':'something'},'data': {'thing': 'stuff', 'something': 'else'}}
	stack = ProcessingStack(state, comm)
	# res_state, result = stack.run()
	result = stack.interpret()
	assert result == 'else'


def test_build_stack_can_differentiate_between_dict_and_expression():
	# our first argument to the 'get_it' expression is a dictionary,
	# not another expression.  it should not be created as a Function.
	expected = ['a', 'dictionary not an expression']
	comm = {
		'get_it': [
			{'just': expected, 'you': 'should not see me in results'},
			'just'
		]
	}

	state = {'job': comm, 'event': {'i_am_nested':'something'},'data': {'thing': 'stuff', 'something': 'else'}}
	stack = ProcessingStack(state, comm)
	# res_state, result = stack.run()
	result = stack.interpret()
	assert result == expected


def test_build_stack_can_build_tree_with_multiple_commands():
	comm1 = {'get_it': ['$$data', 'something']}
	comm2 = {'get_it': ['$$data', 'thing']}
	state = {'job': comm1, 'event': None,'data': {'thing': 'stuff', 'something': 'else'}}

	stack = ProcessingStack(state, comm1, comm2)
	# res_state, result = stack.run()
	result = stack.interpret()
	assert result == ['else', 'stuff']


# def test__build_stack_returns_list():
# 	expected = ['a', 'dictionary not an expression']
# 	comm = {
# 		'get_it': [
# 			{'just': expected, 'you': 'should not see me in results'},
# 			'just'
# 		]
# 	}

# 	state = {'job': comm, 'event': {'i_am_nested':'something'},'data': {'thing': 'stuff', 'something': 'else'}}
# 	stack = ProcessingStack(state, comm)


