import abc


class UOneAbstractExpression(metaclass=abc.ABCMeta):
    """
    Declare an abstract Interpret operation that is common to all nodes
    in the abstract syntax tree.
    """

    @abc.abstractmethod
    def interpret(self):
        pass


class UOneNonterminalExpression(UOneAbstractExpression):
    """
    Implement an Interpret operation for nonterminal symbols in the grammar.
    """

    def __init__(self, expression):
        self._expression = expression

    def interpret(self):
        self._expression.interpret()


class UOneTerminalExpression(UOneAbstractExpression):
    """
    Implement an Interpret operation associated with terminal symbols in
    the grammar.
    """

    def interpret(self):
        pass
