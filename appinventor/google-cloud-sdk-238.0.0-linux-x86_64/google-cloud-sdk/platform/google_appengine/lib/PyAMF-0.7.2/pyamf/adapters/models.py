"""
A large number of adapters interact with models of various kinds.

SQLAlchemy, Django, Google AppEngine etc.

This provides a place to have common functionality for interacting with those
types of models.

@since: 0.7.0
"""

#: mapping of Model property class -> handler
#: See L{register_property_decoder}
_property_decoders = {}

#: mapping of Model property class -> handler
#: See L{register_property_encoder}
_property_encoders = {}


def register_property_decoder(prop_class, replace=False):
    """
    Decorator that will call the handler when decoding an attribute of a model.

    The handler will be given 2 parameters: The property instance being decoded
    and the value of the property that has been decoded. It is the job of the
    handler to return the value.

    @param prop_class: A L{db.Property} class.
    @param replace: Whether to replace an existing handler for a given
        property.
    @since: 0.7.0
    """
    def wrapped(handler):
        if not replace and prop_class in _property_decoders:
            raise KeyError('Handler %r already exists for prop %r' % (
                _property_decoders[prop_class],
                prop_class,
            ))

        _property_decoders[prop_class] = handler

        return handler

    return wrapped


def register_property_encoder(prop_class, replace=False):
    """
    Decorator that will call the handler when decoding an attribute of a model.

    The handler will be given 2 parameters: The property instance being encoded
    and the value of the property that has been decoded. It is the job of the
    handler to return the value.

    @param prop_class: A L{db.Property} class.
    @param replace: Whether to replace an existing handler for a given
        property.
    @since: 0.7.0
    """
    def wrapped(handler):
        if not replace and prop_class in _property_encoders:
            raise KeyError('Handler %r already exists for prop %r' % (
                _property_encoders[prop_class],
                prop_class,
            ))

        _property_encoders[prop_class] = handler

        return handler

    return wrapped


def decode_model_property(obj, prop, value):
    """
    """
    handler = _property_decoders.get(prop.__class__, None)

    if handler:
        return handler(obj, prop, value)

    for model_prop, handler in _property_decoders.iteritems():
        if isinstance(prop, model_prop):
            _property_decoders[prop.__class__] = handler

            return handler(obj, prop, value)

    return value


def encode_model_property(obj, prop, value):
    """
    """
    handler = _property_encoders.get(prop.__class__, None)

    if handler:
        return handler(obj, prop, value)

    for model_prop, handler in _property_encoders.iteritems():
        if isinstance(prop, model_prop):
            _property_encoders[prop.__class__] = handler

            return handler(obj, prop, value)

    return value


def decode_model_properties(obj, model_properties, attrs):
    """
    Given a dict of model properties (name -> property instance), and a set
    of decoded attributes (name -> value); apply each handler to a property, if
    available.
    """
    property_attrs = [k for k in attrs if k in model_properties]

    for name in property_attrs:
        prop = model_properties[name]

        attrs[name] = decode_model_property(obj, prop, attrs[name])

    return attrs


def encode_model_properties(obj, model_properties, attrs):
    """
    Given a dict of model properties (name -> property instance), and a set
    of encodable attributes (name -> value); apply each handler to a property,
    if available.
    """
    property_attrs = [k for k in attrs if k in model_properties]

    for name in property_attrs:
        prop = model_properties[name]

        attrs[name] = encode_model_property(obj, prop, attrs[name])

    return attrs
