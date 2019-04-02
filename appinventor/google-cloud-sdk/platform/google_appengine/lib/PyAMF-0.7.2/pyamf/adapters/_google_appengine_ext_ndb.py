# Copyright (c) The PyAMF Project.
# See LICENSE.txt for details.

"""
Google App Engine ndb adapter module.
"""

import datetime

from google.appengine.ext import ndb
from google.appengine.ext.ndb import polymodel

import pyamf
from pyamf.adapters import util, models as adapter_models, gae_base


NDB_STUB_NAME = 'gae_ndb_stub'


class NDBReferenceCollection(gae_base.EntityReferenceCollection):
    """
    This helper class holds a dict of klass to key/objects loaded from the
    Datastore.

    @since: 0.4.1
    """

    base_classes = (ndb.Model, ndb.Expando)


class NDBStubCollection(gae_base.StubCollection):
    def fetchEntities(self):
        return dict(zip(self.to_fetch, ndb.get_multi(self.to_fetch)))


class NDBClassAlias(gae_base.BaseDatastoreClassAlias):
    """
    This class contains all the business logic to interact with Google's
    Datastore API's. Any C{ndb.Model} or C{ndb.Expando} classes will use this
    class alias for encoding/decoding.

    We also add a number of indexes to the encoder context to aggressively
    decrease the number of Datastore API's that we need to complete.
    """

    base_classes = (ndb.Model, polymodel.PolyModel)
    context_stub_name = NDB_STUB_NAME

    def getEntityRefCollection(self, codec):
        return get_ndb_context(codec)

    def makeStubCollection(self):
        return NDBStubCollection()

    def encode_key(self, obj):
        key = obj.key

        if not key:
            return None

        return key.urlsafe()

    def decode_key(self, key):
        return ndb.Key(urlsafe=key)

    def getCustomProperties(self):
        props = {}
        # list of property names that are considered read only
        read_only_props = []
        repeated_props = {}
        non_repeated_props = {}
        # list of property names that are computed
        computed_props = {}

        for name, prop in self.klass._properties.iteritems():
            props[name] = prop

            if prop._repeated:
                repeated_props[name] = prop
            else:
                non_repeated_props[name] = prop

            if isinstance(prop, ndb.ComputedProperty):
                computed_props[name] = prop

        if issubclass(self.klass, polymodel.PolyModel):
            del props['class']

        # check if the property is a defined as a computed property. These
        # types of properties are read-only
        for name, value in self.klass.__dict__.iteritems():
            if isinstance(value, ndb.ComputedProperty):
                read_only_props.append(name)

        self.encodable_properties.update(props.keys())
        self.decodable_properties.update(props.keys())
        self.readonly_attrs.update(read_only_props)

        if computed_props:
            self.decodable_properties.difference_update(computed_props.keys())

        self.model_properties = props or None
        self.repeated_properties = repeated_props or None
        self.non_repeated_properties = non_repeated_props or None
        self.computed_properties = computed_props or None

    def getDecodableAttributes(self, obj, attrs, codec=None):
        attrs = super(NDBClassAlias, self).getDecodableAttributes(
            obj, attrs, codec=codec
        )

        if self.repeated_properties:
            for name, prop in self.repeated_properties.iteritems():
                try:
                    value = attrs[name]
                except KeyError:
                    continue

                if not value:
                    attrs[name] = []

                    continue

                for idx, val in enumerate(value):
                    value[idx] = adapter_models.decode_model_property(
                        obj,
                        prop,
                        val
                    )

                attrs[name] = value

        if self.non_repeated_properties:
            adapter_models.decode_model_properties(
                obj,
                self.non_repeated_properties,
                attrs,
            )

        return attrs

    def encode_property(self, obj, prop, value):
        if not prop._repeated:
            return adapter_models.encode_model_property(
                obj,
                prop,
                value
            )

        if not value:
            return []

        for idx, val in enumerate(value):
            value[idx] = adapter_models.encode_model_property(
                obj,
                prop,
                val
            )

        return value

    def getEncodableAttributes(self, obj, codec=None):
        attrs = super(NDBClassAlias, self).getEncodableAttributes(
            obj, codec=codec
        )

        if self.model_properties:
            for name in self.encodable_properties:
                prop = self.model_properties.get(name, None)

                if not prop:
                    continue

                try:
                    value = attrs[name]
                except KeyError:
                    value = self.getAttribute(obj, name, codec=codec)

                attrs[name] = self.encode_property(
                    obj,
                    prop,
                    value
                )

        if isinstance(obj, ndb.Expando):
            for name, prop in obj._properties.iteritems():
                if name in self.model_properties:
                    continue

                value = self.getAttribute(obj, name, codec=codec)

                attrs[name] = self.encode_property(
                    obj,
                    prop,
                    value
                )

        return attrs


def get_ndb_context(context):
    """
    Returns a reference to the C{gae_ndb_objects} on the context. If it doesn't
    exist then it is created.

    @param context: The context to load the C{gae_ndb_objects} index from.
    @return: The C{gae_ndb_objects} index reference.
    @rtype: Instance of L{GAEReferenceCollection}
    @since: 0.4.1
    """
    try:
        return context['gae_ndb_context']
    except KeyError:
        r = context['gae_ndb_context'] = NDBReferenceCollection()

        return r


def encode_ndb_instance(obj, encoder=None):
    """
    The GAE Datastore creates new instances of objects for each get request.
    This is a problem for PyAMF as it uses the id(obj) of the object to do
    reference checking.

    We could just ignore the problem, but the objects are conceptually the
    same so the effort should be made to attempt to resolve references for a
    given object graph.

    We create a new map on the encoder context object which contains a dict of
    C{object.__class__: {key1: object1, key2: object2, .., keyn: objectn}}. We
    use the datastore key to do the reference checking.

    @since: 0.4.1
    """
    if not obj.key or not obj.key.id():
        encoder.writeObject(obj)

        return

    referenced_object = _get_by_class_key(
        encoder,
        obj.__class__,
        obj.key,
        obj
    )

    if not referenced_object:
        encoder.writeElement(None)

        return

    encoder.writeObject(referenced_object)


def encode_ndb_key(key, encoder=None):
    """
    When encountering an L{ndb.Key} instance, find the entity in the datastore
    and encode that.
    """
    klass = ndb.Model._kind_map.get(key.kind())

    referenced_object = _get_by_class_key(
        encoder,
        klass,
        key,
    )

    if not referenced_object:
        encoder.writeElement(None)

        return

    encoder.writeObject(referenced_object)


def _get_by_class_key(codec, klass, key, obj=None):
    gae_objects = get_ndb_context(codec.context.extra)

    try:
        return gae_objects.get(klass, key)
    except KeyError:
        if not obj:
            obj = key.get()

        gae_objects.set(klass, key, obj)

        return obj


@adapter_models.register_property_decoder(ndb.KeyProperty)
def decode_key_property(obj, prop, value):
    if not value:
        return None

    return ndb.Key(urlsafe=value)


@adapter_models.register_property_decoder(ndb.DateProperty)
def decode_time_property(obj, prop, value):
    if not hasattr(value, 'date'):
        return value

    return value.date()


@adapter_models.register_property_decoder(ndb.FloatProperty)
def decode_float_property(obj, prop, value):
    if isinstance(value, (int, long)):
        return float(value)

    return value


@adapter_models.register_property_decoder(ndb.IntegerProperty)
def decode_int_property(obj, prop, value):
    if isinstance(value, float):
        long_val = long(value)

        # only convert the type if there is no mantissa - otherwise
        # let the chips fall where they may
        if long_val == value:
            return long_val

    return value


@adapter_models.register_property_encoder(ndb.KeyProperty)
def encode_key_property(obj, prop, value):
    if not hasattr(value, 'urlsafe'):
        return value

    return value.urlsafe()


@adapter_models.register_property_encoder(ndb.TimeProperty)
def encode_time_property(obj, prop, value):
    # PyAMF supports datetime.datetime objects and won't decide what date to
    # add to this time value. Users will have to figure it out themselves
    raise pyamf.EncodeError('ndb.TimeProperty is not supported by PyAMF')


@adapter_models.register_property_encoder(ndb.DateProperty)
def encode_date_property(obj, prop, value):
    if not value:
        return value

    return datetime.datetime.combine(
        value,
        datetime.time(0, 0, 0)
    )


def post_ndb_process(payload, context):
    """
    """
    stubs = context.get(NDB_STUB_NAME, None)

    if not stubs:
        return payload

    stubs.transform()

    return payload


# small optimisation to compile the ndb.Model base class
if hasattr(ndb.model, '_NotEqualMixin'):
    not_equal_mixin = pyamf.register_class(ndb.model._NotEqualMixin)
    not_equal_mixin.compile()

    del not_equal_mixin

# initialise the module here: hook into pyamf
pyamf.register_alias_type(NDBClassAlias, ndb.Model, ndb.Expando)
pyamf.add_type(ndb.Query, util.to_list)
pyamf.add_type(ndb.Model, encode_ndb_instance)
pyamf.add_post_decode_processor(post_ndb_process)
pyamf.add_type(ndb.Key, encode_ndb_key)
