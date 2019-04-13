# Copyright (c) The PyAMF Project.
# See LICENSE.txt for details.

"""
Google App Engine adapter module.

Sets up basic type mapping and class mappings for using the Datastore API
in Google App Engine.

@see: U{Datastore API on Google App Engine<http://
    code.google.com/appengine/docs/python/datastore>}
@since: 0.3.1
"""

import logging

from google.appengine.ext import db
from google.appengine.ext.db import polymodel

import pyamf
from pyamf.adapters import util, models as adapter_models, gae_base

__all__ = [
    'DataStoreClassAlias',
]

XDB_CONTEXT_NAME = 'gae_xdb_context'
XDB_STUB_NAME = 'gae_xdb_stubs'


class XDBReferenceCollection(gae_base.EntityReferenceCollection):
    base_classes = (db.Model, db.Expando)


class XDBStubCollection(gae_base.StubCollection):
    def fetchEntities(self):
        return dict(zip(self.to_fetch, db.get(self.to_fetch)))


class DataStoreClassAlias(gae_base.BaseDatastoreClassAlias):
    """
    This class contains all the business logic to interact with Google's
    Datastore API's. Any C{db.Model} or C{db.Expando} classes will use this
    class alias for encoding/decoding.

    We also add a number of indexes to the encoder context to aggressively
    decrease the number of Datastore API's that we need to complete.

    @ivar properties: A mapping of attribute -> property instance.
    @ivar reference_properties: A mapping of attribute -> db.ReferenceProperty
        which hold special significance when en/decoding.
    """

    base_classes = (db.Model, polymodel.PolyModel)
    context_stub_name = XDB_STUB_NAME

    def encode_key(self, obj):
        if not obj.is_saved():
            return None

        return unicode(obj.key())

    def decode_key(self, key):
        return db.Key(key)

    def getEntityRefCollection(self, codec):
        return getGAEObjects(codec.context.extra)

    def makeStubCollection(self):
        return XDBStubCollection()

    def getCustomProperties(self):
        self.reference_properties = {}
        self.properties = {}
        reverse_props = []

        for name, prop in self.klass.properties().iteritems():
            self.properties[name] = prop

            if isinstance(prop, db.ReferenceProperty):
                self.reference_properties[name] = prop

        if issubclass(self.klass, polymodel.PolyModel):
            del self.properties['_class']

        # check if the property is a defined as a collection_name. These types
        # of properties are read-only and the datastore freaks out if you
        # attempt to meddle with it. We delete the attribute entirely ..
        for name, value in self.klass.__dict__.iteritems():
            if isinstance(value, db._ReverseReferenceProperty):
                reverse_props.append(name)

        self.encodable_properties.update(self.properties.keys())
        self.decodable_properties.update(self.properties.keys())
        self.readonly_attrs.update(reverse_props)

        if not self.reference_properties:
            self.reference_properties = None

        if not self.properties:
            self.properties = None

    def getAttribute(self, obj, attr, codec=None):
        if codec is None:
            return super(DataStoreClassAlias, self).getAttribute(
                obj, attr, codec=codec,
            )

        if not self.reference_properties:
            return super(DataStoreClassAlias, self).getAttribute(
                obj, attr, codec=codec,
            )

        try:
            prop = self.reference_properties[attr]
        except KeyError:
            return super(DataStoreClassAlias, self).getAttribute(
                obj, attr, codec=codec,
            )

        key = prop.get_value_for_datastore(obj)

        if key is None:
            return super(DataStoreClassAlias, self).getAttribute(
                obj, attr, codec=codec,
            )

        klass = prop.reference_class
        entity_ref_collection = self.getEntityRefCollection(codec)

        try:
            return entity_ref_collection.get(klass, key)
        except KeyError:
            pass

        try:
            ref_obj = super(DataStoreClassAlias, self).getAttribute(
                obj, attr, codec=codec,
            )
        except db.ReferencePropertyResolveError:
            logging.warn(
                'Attempted to get %r on %r with key %r',
                attr,
                type(obj),
                key
            )

            return None

        entity_ref_collection.set(klass, key, ref_obj)

        return ref_obj

    def getEncodableAttributes(self, obj, codec=None):
        attrs = super(DataStoreClassAlias, self).getEncodableAttributes(
            obj,
            codec=codec
        )

        for attr in obj.dynamic_properties():
            attrs[attr] = self.getAttribute(obj, attr, codec=codec)

        if self.properties:
            for name in self.encodable_properties:
                prop = self.properties.get(name, None)

                if not prop:
                    continue

                try:
                    value = attrs[name]
                except KeyError:
                    value = self.getAttribute(obj, name, codec=codec)

                attrs[name] = adapter_models.encode_model_property(
                    obj,
                    prop,
                    value
                )

        return attrs

    def getDecodableAttributes(self, obj, attrs, codec=None):
        attrs = super(DataStoreClassAlias, self).getDecodableAttributes(
            obj,
            attrs,
            codec=codec
        )

        if self.properties:
            adapter_models.decode_model_properties(obj, self.properties, attrs)

        return attrs


def getGAEObjects(context):
    """
    Returns a reference to the C{gae_objects} on the context. If it doesn't
    exist then it is created.

    @param context: The context to load the C{gae_objects} index from.
    @return: The C{gae_objects} index reference.
    @rtype: Instance of L{GAEReferenceCollection}
    @since: 0.4.1
    """
    ref_collection = context.get(XDB_CONTEXT_NAME, None)

    if ref_collection:
        return ref_collection

    ret = context[XDB_CONTEXT_NAME] = XDBReferenceCollection()

    return ret


def encode_xdb_entity(obj, encoder=None):
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
    if not obj.is_saved():
        encoder.writeObject(obj)

        return

    kls = obj.__class__
    s = obj.key()

    gae_objects = getGAEObjects(encoder.context.extra)

    try:
        referenced_object = gae_objects.get(kls, s)
    except KeyError:
        referenced_object = obj
        gae_objects.set(kls, s, obj)

    if not referenced_object:
        encoder.writeElement(None)
    else:
        encoder.writeObject(referenced_object)


def encode_xdb_key(key, encoder=None):
    """
    Convert the `db.Key` to it's entity and encode it.
    """
    gae_objects = getGAEObjects(encoder.context.extra)

    klass = db.class_for_kind(key.kind())

    try:
        referenced_object = gae_objects.get(klass, key)
    except KeyError:
        referenced_object = db.get(key)
        gae_objects.set(klass, key, referenced_object)

    if not referenced_object:
        encoder.writeElement(None)
    else:
        encoder.writeObject(referenced_object)


@adapter_models.register_property_decoder(db.FloatProperty)
def decode_float_property(obj, prop, value):
    if isinstance(value, (int, long)):
        return float(value)

    return value


@adapter_models.register_property_decoder(db.IntegerProperty)
def decode_integer_property(obj, prop, value):
    if isinstance(value, float):
        x = int(value)

        # only convert the type if there is no mantissa - otherwise let the
        # chips fall where they may
        if x == value:
            return x

    return value


@adapter_models.register_property_decoder(db.ListProperty)
def decode_list_property(obj, prop, value):
    if value is None:
        return []

    # there is an issue with large ints and ListProperty(int) AMF leaves
    # ints > amf3.MAX_29B_INT as floats db.ListProperty complains pretty
    # hard in this case so we try to work around the issue.
    if prop.item_type in (int, long):
        for i, x in enumerate(value):
            if isinstance(x, float):
                y = int(x)

                # only convert the type if there is no mantissa
                # otherwise let the chips fall where they may
                if x == y:
                    value[i] = y

    return value


@adapter_models.register_property_decoder(db.DateProperty)
def decode_date_property(obj, prop, value):
    if not hasattr(value, 'date'):
        return value

    # DateProperty fields expect specific types of data
    # whereas PyAMF only decodes into datetime.datetime
    # objects.
    return value.date()


@adapter_models.register_property_decoder(db.TimeProperty)
def decode_time_property(obj, prop, value):
    if not hasattr(value, 'time'):
        return value

    # TimeProperty fields expect specific types of data
    # whereas PyAMF only decodes into datetime.datetime
    # objects.
    return value.time()


def transform_xdb_stubs(payload, context):
    """
    Called when a successful decode has been performed. Transform the stubs
    within the payload to proper db.Model instances.
    """
    stubs = context.get(XDB_STUB_NAME, None)

    if not stubs:
        return payload

    stubs.transform()

    return payload


# initialise the module here: hook into pyamf
pyamf.register_alias_type(DataStoreClassAlias, db.Model)
pyamf.add_type(db.Query, util.to_list)
pyamf.add_type(db.Key, encode_xdb_key)
pyamf.add_type(db.Model, encode_xdb_entity)

pyamf.add_post_decode_processor(transform_xdb_stubs)
