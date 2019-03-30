import collections

import pyamf


class EntityStub(object):
    """
    This class is a placeholder for a Google AppEngine datastore entity while
    PyAMF is decoding. The key of the entity is used as the reference check,
    not the `id()`.

    This stub is added to a map which is then converted to the correct type
    as part of the finalise/post process step.
    """


class EntityReferenceCollection(dict):
    """
    This helper class holds a map of klass -> key -> entities loaded from the
    Datastore.

    @since: 0.4.1
    """

    # list of classes that this ref collection supports, must be implemented
    # by subclasses
    base_classes = None

    def _getClass(self, klass):
        if not issubclass(klass, self.base_classes):
            raise TypeError('expected one of %r class, got %s' % (
                self.base_classes,
                klass,
            ))

        return self.setdefault(klass, {})

    def get(self, klass, key):
        """
        Return an instance based on klass/key.

        If an instance cannot be found then C{KeyError} is raised.

        @param klass: The class of the instance.
        @param key: The key of the instance.
        @return: The instance linked to the C{klass}/C{key}.
        @rtype: Instance of L{klass}.
        """
        d = self._getClass(klass)

        return d[key]

    def set(self, klass, key, obj):
        """
        Adds an object to the collection, based on klass and key.

        @param klass: The class of the object.
        @param key: The datastore key of the object.
        @param obj: The loaded instance from the datastore.
        """
        d = self._getClass(klass)

        d[key] = obj


class StubCollection(object):
    """
    A mapping of `EntityStub` instances to key/id. As the AMF graph is
    decoded, L{EntityStub} instances are created as markers to be replaced
    in the finalise stage of decoding. At that point all the ndb/xdb entities
    are fetched from the datastore and hydrated in to proper Python objects and
    the stubs are transformed in to this objects so that referential integrity
    is maintained.

    A complete hack but because of the flexibility of Python but it works ..
    """

    def __init__(self):
        self.stubs = collections.OrderedDict()
        self.to_fetch = []
        self.fetched_entities = None

    def fetchEntities(self):  # pragma: nocover
        """
        Fetches all the `to_fetch` entities from the datastore.
        """
        raise NotImplementedError

    def add(self, stub, alias, key):
        """
        Add a stub to this collection.

        @param stub: The L{ModelStub} instance.
        @param alias: The L{pyamf.ClassAlias} linked to this stub.
        @param attrs: The decoded name -> value mapping of attributes.
        @param key: The key string if known.
        """
        if stub not in self.stubs:
            self.stubs[stub] = (alias.klass, key)

        if key:
            self.to_fetch.append(key)

    def transformStub(self, stub, klass, key):
        attrs = stub.__dict__.copy()
        stub.__dict__.clear()
        stub.__class__ = klass

        for k, v in attrs.items():
            if not isinstance(v, EntityStub):
                continue

            self.transform(v)

        if key is None:
            stub.__init__(**attrs)

            return

        ds_entity = self.fetched_entities.get(key, None)

        if not ds_entity:
            attrs['key'] = key
            stub.__init__(**attrs)
        else:
            stub.__dict__.update(ds_entity.__dict__)

            for k, v in attrs.items():
                setattr(stub, k, v)

    def transform(self, stub=None):
        if self.fetched_entities is None:
            self.fetched_entities = self.fetchEntities()

        if stub is not None:
            stub, klass, key = self.stubs.pop(stub)

            self.transformStub(stub, klass, key)

            return

        for stub, (klass, key) in self.stubs.iteritems():
            self.transformStub(stub, klass, key)


class BaseDatastoreClassAlias(pyamf.ClassAlias):
    """
    """

    base_classes = None
    context_stub_name = None

    # The name of the attribute used to represent the key
    KEY_ATTR = '_key'

    def _compile_base_class(self, klass):
        if klass in self.base_classes:
            # can't compile these classes, so this is as far as we go
            return

        pyamf.ClassAlias._compile_base_class(self, klass)

    def _finalise_compile(self):
        pyamf.ClassAlias._finalise_compile(self)

        self.shortcut_decode = False

    def createInstance(self, codec=None):
        """
        Called when PyAMF needs an object to use as part of the decoding
        process. This is sort of a hack but an POPO is returned which can then
        be transformed in to the db.Model instance.
        """
        return EntityStub()

    def getEntityRefCollection(self, codec):  # pragma: nocover
        raise NotImplementedError

    def encode_key(self, obj):  # pragma: nocover
        """
        Returns an encoded version of the key of the entity if there is one.
        """
        raise NotImplementedError

    def decode_key(self, key):  # pragma: nocover
        """
        Given an encoded version of a key, decode it and return the Key
        instance.
        """
        raise NotImplementedError

    def makeStubCollection(self):  # pragma: nocover
        raise NotImplementedError

    def getStubCollection(self, codec):
        extra = codec.context.extra

        stubs = extra.get(self.context_stub_name, None)

        if not stubs:
            stubs = extra[self.context_stub_name] = self.makeStubCollection()

        return stubs

    def getEncodableAttributes(self, obj, codec=None):
        attrs = super(BaseDatastoreClassAlias, self).getEncodableAttributes(
            obj, codec=codec
        )

        for k in attrs.keys()[:]:
            if k.startswith('_'):
                del attrs[k]

        attrs[self.KEY_ATTR] = self.encode_key(obj)

        return attrs

    def getDecodableAttributes(self, obj, attrs, codec=None):
        key = attrs.pop(self.KEY_ATTR, None)

        if key:
            key = self.decode_key(key)

        attrs = super(BaseDatastoreClassAlias, self).getDecodableAttributes(
            obj,
            attrs,
            codec=codec
        )

        stubs = self.getStubCollection(codec)

        stubs.add(obj, self, key)

        return attrs
