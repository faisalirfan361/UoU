def serialize_helper(obj):
    if isinstance(obj, set):
        return list(obj)

    return obj
