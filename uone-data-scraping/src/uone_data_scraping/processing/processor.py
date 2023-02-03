from . import modifiers, dotmap


def process_it(command, context, event=None, data=None, *proc_args):
    """Process arbitrary commands on a context object, given an event and/or data and/or processing args

    Params:
    -------
    - command : dict(str:list(str|dict|obj))
        A dictionary whos key is a function name and the value of the single-keyed dictionary is
        always a list of params for the command.  The command name _must_ exist in the `modifiers`.
        The arguments for the command are all strings.  These strings can be references to real objects
        and data or they can be string literals, used as actual arguments to the `command`.
        command structure looks something like this:

            {<command name>: [param,...]}

        which might look like this, in a real world example:

            {'cache_it': ['client-123', 'nic', 'agentId', '$$data', '*']}

        which would run the `modifiers.cache_it` function, with these params:
            'client-123', 'nic', 'agentId', `data`, '*'

        notice here that the '$$data' argument is a reference.  Also notice that when it was passed
        to the `cache_it` function, it was swapped out for the actual `data` object that we received
        in the parameters passed to this invocation of the `process_it` function.
        See the Notes section for more info...

    - context : object (usually dict)
        An object which is passed to the function for which the given `command` references. This allows
        the arbitrary function from the modifiers to have a place to set variables and pass a context
        to other functions and back to the main process, if needed.
    - event : dict optional
        An object which can be passed to the function given in the params, usually an `event` object
        from a lambda or similar.
    - data : dict optional
        An object which can be passed to the function given in the params for the `command`.  This is
        another context item that we can use to get, set, reference, etc values on/from.  Data is usually
        the object for which this process function was called.  Data is usually modified or referenced because
        it is the result of a scrape and we want to know info in that result.  We can also reference the
        data object for information on which cache to send the record to.  Another way `data` is used is
        to be enriched by the function given in the `command` arg.
    - proc_args : kwargs collection
        Arbitrary key-value pairs to be passed along to the processing function.

    Returns:
    --------
    object from the function given in the `command` argument : object

    Notes:
    ------
    `context`, `data` and `event` are all used to replace references from the `command` argument's
    parameters list.  In the example above, '$$data' gets turned into the `data` object.  If you passed
    '$$event' as one of the strings in the parameters list in the `command` values, it would be replaced
    with the `event` object that was passed to this function or the `None` object if no `event` was passed.

    """
    scopes = {
        '$$context':context,
        '$$config':context,
        '$$event':event,
        '$$data':data
    }
    args = list(command.values())[0]
    comm = list(command.keys())[0]
    print(f"Gathered command: {comm} and args: {args}.")

    compiled = []
    for arg in args:
        if isinstance(arg, dict):
            arg = scopes[arg] if isinstance(arg, str) and arg in scopes.keys() else arg
            subparams = [scopes[a] if isinstance(a, str) and a in scopes.keys() else a for a in list(arg.values())[0]]
            res = process_it(arg, context, event, data, *subparams)
            print(f"Compiled {arg} to: {res}")
            compiled.append(res)
        else:
            # arg = scopes[arg] if isinstance(arg, str) and arg in scopes.keys() else arg
            availables = scopes
            if isinstance(arg, str) and '$$modifiers' in arg:
                funcname = arg.split('$$modifiers.')[-1]

                if funcname in dir(modifiers):
                    availables[arg] = getattr(modifiers, funcname)
                else:
                    availables[arg] = modifiers

            try:
                arg = availables.get(arg, arg)
            except TypeError:
                arg = arg

            print(f"Found arg: {arg}")
            compiled.append(arg)

    # run the actual function using the params that were recursively run through process_it
    final_func = getattr(modifiers, comm)
    return final_func(*compiled)
